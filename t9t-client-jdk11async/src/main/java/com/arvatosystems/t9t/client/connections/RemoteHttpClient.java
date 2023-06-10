/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.client.connections;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.HttpPostResponseObject;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.util.ApplicationException;

class RemoteHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteHttpClient.class);

    protected final AtomicInteger threadCounter = new AtomicInteger();
    protected final AtomicInteger nextConnection = new AtomicInteger(0);
    protected final int poolSize;
    protected final List<SingleConnection> httpClients;
    private final ExecutorService executorService;

    protected void statisticsOut() {
        for (final SingleConnection me: httpClients) {
            LOGGER.info("Connection {}: current = {}, peak = {}, total = {}", me.index, me.currentPending.get(), me.peakUse.get(), me.totalUses.get());
        }
    }

    RemoteHttpClient(final int threadPoolSize, final int parallelConnections) {
        LOGGER.info("Creating new HttpClient for remote connections, using {} threads and {} separate instances", threadPoolSize, parallelConnections);
        executorService = Executors.newFixedThreadPool(threadPoolSize, (r) -> {
            final String threadName = "t9t-http-async-" + threadCounter.incrementAndGet();
            LOGGER.info("Launching thread {} of {} for asynchronous http response processing", threadName, threadPoolSize);
            return new Thread(r, threadName);
        });

        httpClients = new ArrayList<>(parallelConnections);
        poolSize = parallelConnections;
        for (int i = 0; i < parallelConnections; ++i) {
            httpClients.add(new SingleConnection(i, executorService));
        }
    }

    protected SingleConnection pickConnectionToUse() {
        // first choice is the next one, round robin,
        int initialChoiceIndex = nextConnection.incrementAndGet();
        if (initialChoiceIndex > 1_000_000) {
            nextConnection.set(0);
            executorService.submit(() -> statisticsOut());
        }
        initialChoiceIndex = initialChoiceIndex % poolSize;
        final SingleConnection initialChoice = httpClients.get(initialChoiceIndex);
        // use this, unless we find a connection which is a lot less active
        final int pendingForinitialChoice = initialChoice.currentPending.get();
        if (pendingForinitialChoice < 20) {
            return initialChoice;
        }
        final int limit = pendingForinitialChoice / 2;  // at least 10...
        // fall back for selection of initial best
        for (int i = 0; i < poolSize; ++i) {
            final SingleConnection alternateChoice = httpClients.get(i);
            if (alternateChoice.currentPending.get() <= limit) {
                return alternateChoice;
            }
        }
        return initialChoice;
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

    public CompletableFuture<ServiceResponse> doIO(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final HttpRequest httpRq = buildRequest(uri, authentication, request);
        final BodyHandler<byte[]> serializedRequest = HttpResponse.BodyHandlers.ofByteArray();
        final SingleConnection myConnection = pickConnectionToUse();
        final int newHigh = myConnection.currentPending.incrementAndGet();
        if (newHigh > myConnection.peakUse.get()) {
            myConnection.peakUse.set(newHigh);
        }
        final CompletableFuture<HttpResponse<byte[]>> responseF = myConnection.httpClient.sendAsync(httpRq, serializedRequest);
        return responseF.thenApply(response -> {
            myConnection.currentPending.decrementAndGet();
            myConnection.totalUses.incrementAndGet();
            return convertResponse(response2object(response), request instanceof AuthenticationRequest);
        });
    }

    private ServiceResponse convertResponse(final HttpPostResponseObject resp, final boolean isAuthentication) {
        if (resp.getHttpReturnCode() / 100 != 2) {
            final BonaPortable response = resp.getResponseObject();
            if (response instanceof ServiceResponse sr) {
                if (!ApplicationException.isOk(sr.getReturnCode())) {
                    // TODO: check if we should just pass back sr
                    return MessagingUtil.createServiceResponse(
                        T9tException.HTTP_ERROR + resp.getHttpReturnCode(),
                        Integer.toString(sr.getReturnCode()) + ": " + (sr.getErrorDetails() != null ? sr.getErrorDetails() + " " : "") + sr.getErrorMessage());
                }
            }
            return MessagingUtil.createServiceResponse(
                    T9tException.HTTP_ERROR + resp.getHttpReturnCode(),
                    resp.getHttpStatusMessage());
        }
        final BonaPortable response = resp.getResponseObject();
        if (response == null) {
            if (isAuthentication) {
                LOGGER.warn("Response object is null for AUTHENTICATION: http code {}, status {}", resp.getHttpReturnCode(), resp.getHttpStatusMessage());
                return MessagingUtil.createServiceResponse(
                        T9tException.GENERAL_AUTH_PROBLEM,
                        AuthenticationResponse.class.getCanonicalName());
            } else {
                LOGGER.warn("Response object is null for GENERAL request: http code {}, status {}", resp.getHttpReturnCode(), resp.getHttpStatusMessage());
                return MessagingUtil.createServiceResponse(
                        T9tException.BAD_REMOTE_RESPONSE,
                        Integer.toString(resp.getHttpReturnCode()));
            }
        }
        if (response instanceof ServiceResponse sr) {
            LOGGER.debug("Received response type {} with return code {}", sr.ret$PQON(), sr.getReturnCode());
            if (!ApplicationException.isOk(sr.getReturnCode())) {
                LOGGER.debug("Error details are {}, message is {}", sr.getErrorDetails(), sr.getErrorMessage());
            }
            return sr;
        }


        LOGGER.error("Response is of wrong type: {}", response.getClass().getCanonicalName());
        return MessagingUtil.createServiceResponse(
                T9tException.GENERAL_EXCEPTION,
                response.getClass().getCanonicalName());
    }
}
