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
package com.arvatosystems.t9t.out.be.async;

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;

public abstract class AbstractPostSenderJdk11Json extends AbstractPostSenderJdk11<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostSenderJdk11Json.class);

    protected ObjectMapper objectMapper = null;

    @Override
    public void init(final AsyncQueueDTO myQueue) {
        LOGGER.info("Creating IAsyncSender POST JSON with JDK 11 HttpClient for queue {}", myQueue.getAsyncQueueId());
        super.init(myQueue);
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
    }

    @Override
    protected BodyHandler<String> getBodyHandler() {
        return HttpResponse.BodyHandlers.ofString();
    }

    @Override
    public void close() {
        super.close();
        objectMapper = null;
    }
}
