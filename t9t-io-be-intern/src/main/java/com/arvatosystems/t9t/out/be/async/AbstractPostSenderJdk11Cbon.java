/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.MimeTypes;

/**
 * The PostSender implements a simple client invocation via http POST of the JDK 11 HttpClient, using compact bonaparte serialization.
 */
public abstract class AbstractPostSenderJdk11Cbon extends AbstractPostSenderJdk11<byte[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostSenderJdk11Cbon.class);

    protected CompactByteArrayComposer cbac = null;

    @Override
    public void init(final AsyncQueueDTO myQueue) {
        LOGGER.info("Creating IAsyncSender POST CBON with JDK 11 HttpClient for queue {}", myQueue.getAsyncQueueId());
        super.init(myQueue);
        cbac = new CompactByteArrayComposer(false);
    }

    /**
     * Returns a publisher for the provided data. This implementation converts the BonaPortable into compact Bonaparte serialization.
     */
    protected void addDefaultPublisherForPayload(final HttpRequest.Builder httpRequestBuilder, final BonaPortable payload) throws Exception {
        cbac.reset();
        cbac.writeRecord(payload);
        final byte[] payloadAsBytes = cbac.getBytes();
        cbac.close();
        httpRequestBuilder.POST(BodyPublishers.ofByteArray(payloadAsBytes));
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_TYPE,   MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT,         MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
    }

    @Override
    protected BodyHandler<byte[]> getBodyHandler() {
        return HttpResponse.BodyHandlers.ofByteArray();
    }

    @Override
    public void close() {
        super.close();
        cbac = null;
    }
}
