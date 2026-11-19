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
import de.jpaw.bonaparte.core.ByteArrayParser;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

import com.arvatosystems.t9t.api.codecs.IMessageDecoder;

public class BonaparteRecordDecoder<O extends BonaPortable> implements IMessageDecoder<O, byte[]> {

    @Override
    public O decode(byte[] data, ObjectReference di) throws MessageParserException {
        final ByteArrayParser cbap = new ByteArrayParser(data, 0, -1);
        return (O) cbap.readRecord();
    }
}
