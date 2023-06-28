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
package com.arvatosystems.t9t.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.jpaw.util.ByteArray;

public class JpawByteArraySerializer extends StdSerializer<ByteArray> {
    private static final long serialVersionUID = -4703714519198122029L;

    protected JpawByteArraySerializer() {
        super(ByteArray.class);
    }

    @Override
    public void serialize(final ByteArray b, final JsonGenerator gen, final SerializerProvider x) throws IOException {
        gen.writeString(b.asBase64());
    }
}
