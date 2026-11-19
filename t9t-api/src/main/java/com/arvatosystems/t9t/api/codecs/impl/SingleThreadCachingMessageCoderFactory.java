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

import java.util.HashMap;
import java.util.Map;

import de.jpaw.bonaparte.core.BonaPortable;

import com.arvatosystems.t9t.api.codecs.IMessageDecoder;
import com.arvatosystems.t9t.api.codecs.IMessageEncoder;

public class SingleThreadCachingMessageCoderFactory<D extends BonaPortable, E extends BonaPortable> extends MessageCoderFactory<D, E> {
    private final Map<String, IMessageDecoder<D, byte[]>> decoders = new HashMap<>(12);
    private final Map<String, IMessageEncoder<E, byte[]>> encoders = new HashMap<>(12);

    public SingleThreadCachingMessageCoderFactory(Class<D> decoderClass, Class<E> encoderClass) {
        super(decoderClass, encoderClass);
    }

    @Override
    public IMessageEncoder<E, byte[]> getEncoderInstance(String mimeType) {
        IMessageEncoder<E, byte[]> encoder = encoders.get(mimeType);
        if (encoder != null)
            return encoder;
        encoder = super.getEncoderInstance(mimeType);
        if (encoder != null)
            encoders.put(mimeType, encoder);
        return encoder;
    }

    @Override
    public IMessageDecoder<D, byte[]> getDecoderInstance(String mimeType) {
        IMessageDecoder<D, byte[]> decoder = decoders.get(mimeType);
        if (decoder != null)
            return decoder;
        decoder = super.getDecoderInstance(mimeType);
        if (decoder != null)
            decoders.put(mimeType, decoder);
        return decoder;
    }
}
