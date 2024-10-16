/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.out.jpa.impl;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.services.IGenericRemoter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;

/**
 * Implements sync via HTTP to a remote system.
 */
public class GenericRemoterViaDirectHttp implements IGenericRemoter, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericRemoterViaDirectHttp.class);
    private static final Long DEFAULT_TIMEOUT_IN_MS = 20_000L;

    protected final Function<Object, byte[]> serializer;
    protected final UplinkConfiguration uplinkConfig;

    protected AtomicInteger numRequestsPending = new AtomicInteger();
    protected final HttpClient httpClient;

    protected final int requestDelay;
    protected final int maxPending;
    protected final long sleepTimeInMillis;     // sleep to reduce overhead on checking the pending requests.
    protected final long requestTimeoutInMs;    // timeout for all requests off the current connection
    protected final long connectTimeoutInMs;    // timeout for all requests off the current connection
    protected final long requestTimeoutInNanos; // timeout for all requests off the current connection
    protected int counter = 0;

    private Long mixedNvl(final Integer a, final Long b) {
        if (a != null) {
            return Long.valueOf(a.intValue());
        }
        return b;
    }

    public GenericRemoterViaDirectHttp(final String uplinkKey, final Function<Object, byte[]> serializer,
            final int requestDelay, final int maxPending, final long sleepTimeInMillis,
            final Long connectTimeoutInMs, final Long requestTimeoutInMs) {
        uplinkConfig = ConfigProvider.getUplinkOrThrow(uplinkKey);
        this.serializer = serializer;
        this.requestDelay = requestDelay;
        this.maxPending = maxPending;
        this.sleepTimeInMillis = sleepTimeInMillis;
        this.connectTimeoutInMs = connectTimeoutInMs != null ? connectTimeoutInMs : mixedNvl(uplinkConfig.getTimeoutInMs(), DEFAULT_TIMEOUT_IN_MS);
        this.requestTimeoutInMs = requestTimeoutInMs != null ? requestTimeoutInMs : mixedNvl(uplinkConfig.getRequestTimeoutInMs(), this.connectTimeoutInMs);
        this.requestTimeoutInNanos = 1_000_000L * this.requestTimeoutInMs;
        httpClient = HttpClient.newBuilder().version(Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(connectTimeoutInMs)).build();
    }

    @Override
    public void send(final BonaPortable exportData) {
        ++counter;
        final long beginTimestamp = System.nanoTime();
        // block the request if numRequestsPending is more than maxPending
        while (numRequestsPending.get() >= maxPending) {
            long diff = System.nanoTime() - beginTimestamp;
            if (diff >= requestTimeoutInNanos) {
                throw new T9tException(T9tIOException.HTTP_REMOTER_IO_EXCEPTION,
                        "Timeout after waited {} ms on {} requests to be processed.", diff,
                        numRequestsPending.get());
            }
            try {
                Thread.sleep(sleepTimeInMillis);
            } catch (InterruptedException e) {
                LOGGER.error("thread sleep for {} ms being interrupted", sleepTimeInMillis);
            }
        }

        final URI uri = URI.create(uplinkConfig.getUrl());
        final String authentication
          = uplinkConfig.getApiKey() != null ? T9tConstants.HTTP_AUTH_PREFIX_API_KEY + uplinkConfig.getApiKey()
          : uplinkConfig.getBasicAuth() != null ? T9tConstants.HTTP_AUTH_PREFIX_USER_PW + uplinkConfig.getBasicAuth()
          : null;
        final HttpRequest httpRequest = buildJsonRequest(uri, authentication, exportData);
        final BodyHandler<byte[]> serializedRequest = HttpResponse.BodyHandlers.ofByteArray();
        numRequestsPending.incrementAndGet();
        final CompletableFuture<HttpResponse<byte[]>> responseF = httpClient.sendAsync(httpRequest, serializedRequest);
        responseF.thenAccept(response -> {
            numRequestsPending.decrementAndGet();
            final int returnCode = response.statusCode();
            if ((returnCode / 100) != (HttpURLConnection.HTTP_OK / 100)) {
                LOGGER.error("Failed to send async data {} : {}", returnCode, response.body());
                throw new T9tException(T9tIOException.HTTP_REMOTER_IO_EXCEPTION,
                        "Failed to send data to remote with statusCode: " + returnCode);
            }
        });

        // delay after each call
        if (requestDelay > 0) {
            try {
                Thread.sleep(requestDelay);
            } catch (InterruptedException e) {
                LOGGER.error("Error while thread sleep for {} ms", requestDelay);
            }
        }
    }

    public HttpRequest buildJsonRequest(final URI uri, final String authentication, final BonaPortable request) {
        final byte[] data = serializer.apply(request);

        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(uri).version(Version.HTTP_2)
                .POST(BodyPublishers.ofByteArray(data, 0, data.length))
                .timeout(Duration.ofMillis(requestTimeoutInMs));

        if (authentication != null) {
            httpRequestBuilder.header("Authorization", authentication);
        }

        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_TYPE,   MimeTypes.MIME_TYPE_JSON);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT,         MimeTypes.MIME_TYPE_JSON);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CHARSET,        T9tConstants.HTTP_CHARSET_UTF8);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT_CHARSET, T9tConstants.HTTP_CHARSET_UTF8);
        return httpRequestBuilder.build();
    }

    @Override
    public void close() {
        // wait until we see all acks, or fail after some timeout
        final long closeTime = System.nanoTime();

        while (numRequestsPending.get() > 0) {
            final long diff = System.nanoTime() - closeTime;

            if (diff >= requestTimeoutInNanos) {
                throw new T9tException(T9tIOException.HTTP_REMOTER_IO_EXCEPTION,
                        "Timeout on close after waited " + diff / 1000_000L + " ms on " + numRequestsPending.get() + " requests to be processed.");
            }

            if (sleepTimeInMillis > 0L) {
                try {
                    Thread.sleep(sleepTimeInMillis);
                } catch (InterruptedException e) {
                    LOGGER.error("Error while thread sleep for {} ms", sleepTimeInMillis);
                }
            }
        }
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public String getImplementation() {
        return "http";
    }
}
