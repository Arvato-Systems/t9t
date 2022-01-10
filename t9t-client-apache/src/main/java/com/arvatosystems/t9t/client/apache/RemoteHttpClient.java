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
package com.arvatosystems.t9t.client.apache;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;
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
    protected final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();

    RemoteHttpClient(final int threadPoolSize) {
        LOGGER.info("Creating new HttpClient for remote connections, using {} threads", threadPoolSize);
//        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize, (r) -> {
//            final String threadName = "t9t-http-async-" + threadCounter.incrementAndGet();
//            LOGGER.info("Launching thread {} of {} for asynchronous http response processing", threadName, threadPoolSize);
//            return new Thread(r, threadName);
//        });

//        // Start HttpClient.
//        try {
//            httpClient.start();
//        } catch (Exception e) {
//            LOGGER.error("Failed to start httpClient: {}", ExceptionUtil.causeChain(e));
//            throw new T9tException(T9tException.BAD_REMOTE_RESPONSE);
//        }
    }

    public SimpleHttpRequest buildRequest(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final CompactByteArrayComposer bac = new CompactByteArrayComposer(false);
        bac.writeRecord(request);
        bac.close();

        final SimpleHttpRequest rq = SimpleHttpRequests.post(uri);
        rq.setBody(bac.getBytes(), ContentType.create(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE));
//                .version(Version.HTTP_2)
//                .POST(BodyPublishers.ofByteArray(bac.getBuffer(), 0, bac.getLength()))
//                .timeout(Duration.ofSeconds(55));

        if (authentication != null) {
            rq.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, authentication));
        }
//        rq.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.MIME_TYPE_COMPACT_BONAPARTE));
        rq.addHeader(new BasicHeader(HttpHeaders.ACCEPT, MimeTypes.MIME_TYPE_COMPACT_BONAPARTE));
        rq.addHeader(new BasicHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8"));
        rq.addHeader(new BasicHeader(HttpHeaders.CONTENT_ENCODING, "utf-8"));
        rq.addHeader(new BasicHeader("Charset", "utf-8"));
        return rq;
    }

    private HttpPostResponseObject response2object(final SimpleHttpResponse response) {
        final int returnCode = response.getCode();
        LOGGER.debug("*** HTTP Response {}", returnCode);
        if ((returnCode / 100) != (HttpURLConnection.HTTP_OK / 100)) {   // accept 200, 201, etc...
            LOGGER.warn("response is HTTP {} ({})", returnCode, null);
            return new HttpPostResponseObject(returnCode, null, null);
        }
        final byte[] receivedBuffer = response.getBodyBytes();
        final BonaPortable obj = new CompactByteArrayParser(receivedBuffer, 0, -1).readRecord();
        return new HttpPostResponseObject(returnCode, String.valueOf(returnCode), obj);
    }

    public CompletableFuture<HttpPostResponseObject> doIO(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final SimpleHttpRequest httpRq = buildRequest(uri, authentication, request);
        final CompletableFuture<HttpPostResponseObject> respF = new CompletableFuture<>();

        // boilerplate code to satify the fossil...   Java 1.7 is really outdated! Apache httpclient 5.2 will support Java 8 finally!
        httpClient.execute(httpRq, new FutureCallback<SimpleHttpResponse>() {

            @Override
            public void completed(final SimpleHttpResponse response2) {
                respF.complete(response2object(response2));
            }

            @Override
            public void failed(final Exception ex) {
                respF.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                respF.cancel(true);
            }
        });
        return respF;
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
