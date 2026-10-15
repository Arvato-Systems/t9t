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
