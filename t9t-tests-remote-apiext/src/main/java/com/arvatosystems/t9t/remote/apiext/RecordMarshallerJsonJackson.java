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
package com.arvatosystems.t9t.remote.apiext;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;

public class RecordMarshallerJsonJackson implements IMarshaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordMarshallerJsonJackson.class);

    final ObjectMapper objectMapper;

    /** Creates a Jackson based marshaller with the default object mapper. */
    public RecordMarshallerJsonJackson() {
        objectMapper = JacksonTools.createObjectMapper();
    }

    /** Creates a Jackson based marshaller with a specified object mapper. */
    public RecordMarshallerJsonJackson(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getContentType() {
        return MimeTypes.MIME_TYPE_JSON;
    }

    @Override
    public ByteArray marshal(final BonaPortable payload) throws JsonProcessingException {
        return ByteArray.fromString(objectMapper.writeValueAsString(payload));
    }

    @Override
    public BonaPortable unmarshal(final ByteBuilder buffer) {
        return unmarshal(buffer, BonaPortable.class);
    }

    @Override
    public <T extends BonaPortable> T unmarshal(final ByteBuilder buffer, final Class<T> resultClass) {
        try {
            return objectMapper.readValue(buffer.asByteArrayInputStream(), resultClass);
        } catch (final IOException e) {
            LOGGER.error("Jackson exception unmarshalling", e);
            throw new RuntimeException(e);
        }
    }
}
