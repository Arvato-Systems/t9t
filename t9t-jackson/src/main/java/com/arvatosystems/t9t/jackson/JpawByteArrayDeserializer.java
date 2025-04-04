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
package com.arvatosystems.t9t.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import de.jpaw.util.ByteArray;

public class JpawByteArrayDeserializer extends StdDeserializer<ByteArray> {
    private static final long serialVersionUID = -2385203152897522278L;

    protected JpawByteArrayDeserializer() {
        super(ByteArray.class);
    }

    @Override
    public ByteArray deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        byte[] b = p.getBinaryValue();
        return new ByteArray(b);
//        return ByteArray.fromBase64(p.getValueAsString());  // alternative approach
    }
}
