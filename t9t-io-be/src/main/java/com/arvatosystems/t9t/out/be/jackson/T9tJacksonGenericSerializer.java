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
package com.arvatosystems.t9t.out.be.jackson;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Generic Jackson based serializer, used for example for async http output.
 */
public class T9tJacksonGenericSerializer {
    private final ObjectMapper objectMapper;

    /** Creates a serializer for the default configuration (ObjectMapper created here). */
    public T9tJacksonGenericSerializer() {
        this.objectMapper = JacksonTools.createObjectMapper();
    }

    /** Creates a serializer for a specific configuration (ObjectMapper passed in). */
    public T9tJacksonGenericSerializer(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] serialize(final Object object) {
        try {
            return objectMapper.writer().writeValueAsBytes(object);
        } catch (final Exception ex) {
            throw new T9tException(T9tIOException.FAILED_TO_SERIALIZE, ex.getMessage());
        }
    }
}
