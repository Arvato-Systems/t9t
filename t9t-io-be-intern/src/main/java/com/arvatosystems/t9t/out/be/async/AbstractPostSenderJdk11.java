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
package com.arvatosystems.t9t.out.be.async;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataReference;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.out.services.IAsyncSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;

/**
 * The PostSender implements a simple client invocation via http POST of the JDK 11 HttpClient.
 */
public abstract class AbstractPostSenderJdk11 implements IAsyncSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostSenderJdk11.class);
    protected AsyncQueueDTO queue;
    protected String lastUrl = "";
    protected HttpClient defaultHttpClient = null;
    protected ObjectMapper objectMapper = null;

    @Override
    public void init(final AsyncQueueDTO myQueue) {
        LOGGER.info("Creating IAsyncSender POST JSON with JDK 11 HttpClient for queue {}", myQueue.getAsyncQueueId());
        this.queue = myQueue;
        defaultHttpClient = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
        objectMapper = JacksonTools.createObjectMapper();
    }

    /**
     * Returns a publisher for the provided data. This default implementation converts the BonaPortable into JSON.
     */
    protected void addDefaultPublisherForPayload(final HttpRequest.Builder httpRequestBuilder, final BonaPortable payload) throws Exception {
        final String payloadAsString = objectMapper.writeValueAsString(payload);
        httpRequestBuilder.POST(BodyPublishers.ofString(payloadAsString, Charsets.UTF_8));
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_TYPE,   MimeTypes.MIME_TYPE_JSON);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT,         MimeTypes.MIME_TYPE_JSON);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CHARSET,        T9tConstants.HTTP_CHARSET_UTF8);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT_CHARSET, T9tConstants.HTTP_CHARSET_UTF8);
    }

    protected void setHeaders(final HttpRequest.Builder httpRequestBuilder, final DataReference dr) {
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_TYPE, dr.getContentType());
        if (dr.getAccept() != null) {
            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT, dr.getAccept());
        }
        if (dr.getIsCompressed()) {
            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_ENCODING, T9tConstants.HTTP_ENCODING_GZIPPED);
        }
    }

    /**
     * Returns a publisher for the provided data.
     * This implementation is able to handle indirect data references, and falls back to the default publisher.
     */
    protected void addPublisherForPayload(final HttpRequest.Builder httpRequestBuilder, final BonaPortable payload) throws Exception {
        if (payload instanceof DataReference dr) {
            if (dr.getReferenceType() == CommunicationTargetChannelType.FILE) {
                LOGGER.debug("Reading contents of FILE {} as POST payload", dr.getFileOrQueueName());
                final Path path = Path.of(dr.getFileOrQueueName());
                httpRequestBuilder.POST(BodyPublishers.ofFile(path));
                setHeaders(httpRequestBuilder, dr);
                return;
            }
        }
        addDefaultPublisherForPayload(httpRequestBuilder, payload);
    }

    private HttpRequest buildRequest(final URI uri, final String authentication, final BonaPortable payload, int timeout) throws Exception {

        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(uri)
                .version(Version.HTTP_2)
                .timeout(Duration.ofMillis(timeout));
        addPublisherForPayload(httpRequestBuilder, payload);

        if (authentication != null && authentication.trim().length() > 0) {
            httpRequestBuilder.header("Authorization", authentication);
        }

        return httpRequestBuilder.build();
    }

    @Override
    public AsyncHttpResponse send(final AsyncChannelDTO channelDto, final BonaPortable payload, final int timeout, final Long messageObjectRef)
      throws Exception {
        // do external I/O
        final HttpRequest httpRq = buildRequest(new URI(channelDto.getUrl()), channelDto.getAuthParam(), payload, timeout);
        final BodyHandler<String> serializedRequest = HttpResponse.BodyHandlers.ofString();
        final CompletableFuture<HttpResponse<String>> responseF = defaultHttpClient.sendAsync(httpRq, serializedRequest);
        return parseResponse(responseF.get());
    }

    protected AsyncHttpResponse parseResponse(final HttpResponse<String> resp) {
        final AsyncHttpResponse myResponse = new AsyncHttpResponse();
        LOGGER.debug("Received HTTP status {}", resp.statusCode());
        myResponse.setHttpReturnCode(resp.statusCode());
        myResponse.setHttpStatusMessage(null);
        myResponse.setClientReference(MessagingUtil.truncField(resp.body(), AsyncHttpResponse.meta$$clientReference.getLength()));
        return myResponse;
    }

    @Override
    public void close() {
        // the HttpPostClient does not offer a close method, but we can actively remove all references to instances of it
        defaultHttpClient = null;
        objectMapper = null;
    }
}
