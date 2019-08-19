/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.vertx;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.api.codecs.IMessageDecoder;
import de.jpaw.bonaparte.api.codecs.IMessageEncoder;
import de.jpaw.bonaparte.api.codecs.impl.BonaparteRecordDecoder;
import de.jpaw.bonaparte.api.codecs.impl.BonaparteRecordEncoder;
import de.jpaw.bonaparte.api.codecs.impl.CompactRecordDecoder;
import de.jpaw.bonaparte.api.codecs.impl.CompactRecordEncoder;
import de.jpaw.bonaparte.api.codecs.impl.JsonDecoder;
import de.jpaw.bonaparte.api.codecs.impl.JsonEncoder;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;

// the factory becomes multithreaded by just avoiding the caching.
public class MultiThreadMessageCoderFactory2<D extends BonaPortable, E extends BonaPortable> implements IMessageCoderFactory<D, E, byte []> {

    private final Class<D> decoderClass;

    public MultiThreadMessageCoderFactory2(Class<D> decoderClass, Class<E> encoderClass) {
        this.decoderClass = decoderClass;
    }

    // override to add additional methods
    protected IMessageEncoder<E, byte []> createNewEncoderInstance(String mimeType) {
        if (mimeType.equals(MimeTypes.MIME_TYPE_BONAPARTE))
            return new BonaparteRecordEncoder<E>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE))
            return new CompactRecordEncoder<E>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_JSON))
            return new JsonEncoder<E>();
        return null;
    }

    // override to add additional methods
    protected IMessageDecoder<D, byte []> createNewDecoderInstance(String mimeType) {
        if (mimeType.equals(MimeTypes.MIME_TYPE_BONAPARTE))
            return new BonaparteRecordDecoder<D>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE))
            return new CompactRecordDecoder<D>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_JSON))
            return new JsonDecoder<D>(decoderClass);
        return null;
    }

    @Override
    public final IMessageEncoder<E, byte []> getEncoderInstance(String mimeType) {
        return createNewEncoderInstance(mimeType);
    }

    @Override
    public IMessageDecoder<D, byte []> getDecoderInstance(String mimeType) {
        return createNewDecoderInstance(mimeType);
    }
}
