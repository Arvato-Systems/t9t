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
import de.jpaw.bonaparte.core.MimeTypes;

import com.arvatosystems.t9t.api.codecs.IMessageCoderFactory;
import com.arvatosystems.t9t.api.codecs.IMessageDecoder;
import com.arvatosystems.t9t.api.codecs.IMessageEncoder;

public class RecordCoderFactory implements IMessageCoderFactory<BonaPortable, BonaPortable, byte[]> {

    @Override
    public IMessageEncoder<BonaPortable, byte[]> getEncoderInstance(String mimeType) {
        if (mimeType.equals(MimeTypes.MIME_TYPE_BONAPARTE))
            return new BonaparteRecordEncoder<BonaPortable>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE))
            return new CompactRecordEncoder<BonaPortable>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_JSON))
            return new JsonEncoder<BonaPortable>();
        return null;
    }

    @Override
    public IMessageDecoder<BonaPortable, byte[]> getDecoderInstance(String mimeType) {
        if (mimeType.equals(MimeTypes.MIME_TYPE_BONAPARTE))
            return new BonaparteRecordDecoder<BonaPortable>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE))
            return new CompactRecordDecoder<BonaPortable>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_JSON))
            return new JsonDecoder<BonaPortable>(BonaPortable.class);
        return null;
    }
}
