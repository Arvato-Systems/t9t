/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.api.codecs.impl;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.json.JsonParser;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteArray;

import com.arvatosystems.t9t.api.codecs.IMessageDecoder;

public class JsonDecoder<O extends BonaPortable> implements IMessageDecoder<O, byte[]> {

    private final Class<O> decoderClass;

    public JsonDecoder(Class<O> decoderClass) {
        this.decoderClass = decoderClass;
    }

    @Override
    public O decode(byte[] data, ObjectReference di) throws ApplicationException {
        final JsonParser jp = new JsonParser(new String(data, ByteArray.CHARSET_UTF8), false);
        return CastIt.castTo(MapParser.asBonaPortable(jp.parseObject(), di), decoderClass);
    }
}
