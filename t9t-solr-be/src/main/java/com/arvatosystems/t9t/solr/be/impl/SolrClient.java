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
package com.arvatosystems.t9t.solr.be.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.solr.be.ISolrClient;
import com.arvatosystems.t9t.solr.be.impl.response.QueryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.dp.Singleton;

@Singleton
public class SolrClient implements ISolrClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrClient.class);

    protected final AtomicInteger numRequestsPending = new AtomicInteger();
    protected final HttpClient httpClient = HttpClient.newBuilder().version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20)).build();

    protected final ObjectMapper objectMapper = JacksonTools.createObjectMapper();
    private final int processTimeout = 50;

    @Override
    public QueryResponse query(final String url, final QueryBody query) throws IOException, InterruptedException {
        final long beginTimestamp = System.nanoTime();
        final String requestPayload;
        try {
            requestPayload = objectMapper.writeValueAsString(query);

            LOGGER.debug("SOLR expression is {}", requestPayload);
        } catch (final Exception ex) {
            LOGGER.error("Failed to generate JSON data for output", ex);
            throw new T9tException(T9tException.INVALID_REQUEST_PARAMETER_TYPE, ex.getMessage());
        }

        final URI uri = URI.create(url);
        final HttpRequest httpRequest = buildJsonRequest(uri, requestPayload);
        final BodyHandler<String> serializedRequest = HttpResponse.BodyHandlers.ofString();
        final HttpResponse<String> response = httpClient.send(httpRequest, serializedRequest);

        final long endTimestamp = System.nanoTime();
        final int returnCode = response.statusCode();

        if ((returnCode / 100) != (HttpURLConnection.HTTP_OK / 100)) {
            LOGGER.error("Failed to send data: HTTP status returned is {} for request {}, response {}", returnCode, requestPayload, response.body());
            throw new T9tException(T9tException.BAD_REMOTE_RESPONSE, returnCode);
        }
        try {
            final QueryResponse queryResponse = objectMapper.readValue(response.body(), QueryResponse.class);
            queryResponse.setElapsedTime(endTimestamp - beginTimestamp);

            return queryResponse;
        } catch (final Exception ex) {
            LOGGER.error("Failed to generate JSON data for output", ex);
            throw new T9tException(T9tException.RESPONSE_VALIDATION_ERROR, ex.getMessage());
        }
    }

    protected HttpRequest buildJsonRequest(final URI uri, final String requestPayload) {
        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(uri)
            .version(Version.HTTP_2)
            .POST(BodyPublishers.ofString(requestPayload))
            .timeout(Duration.ofSeconds(processTimeout));

        httpRequestBuilder.header("Content-Type", MimeTypes.MIME_TYPE_JSON);
        httpRequestBuilder.header("Accept", MimeTypes.MIME_TYPE_JSON);
        httpRequestBuilder.header("Charset", "utf-8");
        httpRequestBuilder.header("Accept-Charset", "utf-8");
        return httpRequestBuilder.build();
    }
}
