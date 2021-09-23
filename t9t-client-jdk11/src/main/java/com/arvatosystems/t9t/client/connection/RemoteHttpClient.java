/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.client.connection;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.HttpPostResponseObject;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;

class RemoteHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteHttpClient.class);

    private final boolean logText = false;
    private final boolean logHex = false;

    protected final AtomicInteger threadCounter = new AtomicInteger();
    protected final HttpClient httpClient;

    public RemoteHttpClient(int threadPoolSize) {
        LOGGER.info("Creating new HttpClient for remote connections, using {} threads", threadPoolSize);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize, (r) -> {
            final String threadName = "t9t-http-async-" + threadCounter.incrementAndGet();
            LOGGER.info("Launching thread {} of {} for asynchronous http response processing", threadName, threadPoolSize);
            return new Thread(r, threadName);
        });

        httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .executor(executorService)
                .build();
    }

    public HttpRequest buildRequest(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final CompactByteArrayComposer bac = new CompactByteArrayComposer(false);
        bac.writeRecord(request);
        bac.close();

        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(uri)
                .version(Version.HTTP_2)
                .POST(BodyPublishers.ofByteArray(bac.getBuffer(), 0, bac.getLength()))
                .timeout(Duration.ofSeconds(55));

        if (authentication != null)
            httpRequestBuilder.header("Authorization", authentication);

        httpRequestBuilder.header("Content-Type",   MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        httpRequestBuilder.header("Accept",         MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        httpRequestBuilder.header("Charset",        "utf-8");
        httpRequestBuilder.header("Accept-Charset", "utf-8");
        return httpRequestBuilder.build();
    }

    /** Execute the request / response is HttpPostResponseObject object */
    public HttpPostResponseObject doIO2(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final HttpRequest httpRq = buildRequest(uri, authentication, request);
        final HttpResponse<byte[]> response = httpClient.send(httpRq, HttpResponse.BodyHandlers.ofByteArray());
        return response2object(response);
    }

    private HttpPostResponseObject response2object(final HttpResponse<byte[]> response) {
        final int returnCode = response.statusCode();
        LOGGER.debug("*** HTTP Response {}, connection type {}", returnCode, response.version());
        if ((returnCode / 100) != (HttpURLConnection.HTTP_OK / 100)) {   // accept 200, 201, etc...
            LOGGER.warn("response is HTTP {} ({})", returnCode, null);
            return new HttpPostResponseObject(returnCode, null, null);
        }
        final byte[] receivedBuffer = response.body();
        final BonaPortable obj = new CompactByteArrayParser(receivedBuffer, 0, -1).readRecord();
        return new HttpPostResponseObject(returnCode, String.valueOf(returnCode), obj);
    }

    public CompletableFuture<HttpPostResponseObject> doIO(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final HttpRequest httpRq = buildRequest(uri, authentication, request);
        final BodyHandler<byte[]> serializedRequest = HttpResponse.BodyHandlers.ofByteArray();
        final CompletableFuture<HttpResponse<byte[]>> responseF = httpClient.sendAsync(httpRq, serializedRequest);
        return responseF.thenApply(response -> { return response2object(response); });
    }

    protected void requestLogger(final String pqon, final ByteArray serializedRequest) {
        LOGGER.trace("{} serialized as {} bytes for MIME type {}", pqon, serializedRequest.length(), MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        if (logText)
            LOGGER.debug("Request is <{}>", serializedRequest.toString());
        if (logHex)
            LOGGER.debug(serializedRequest.hexdump(0, 0));
    }

    protected void responseLogger(final ByteBuilder serializedResponse) {
        LOGGER.trace("retrieved {} bytes response", serializedResponse.length());
        if (logText)
            LOGGER.debug("Response is <{}>", serializedResponse.toString());
        if (logHex)
            LOGGER.debug(serializedResponse.hexdump(0, 0));
    }
}
