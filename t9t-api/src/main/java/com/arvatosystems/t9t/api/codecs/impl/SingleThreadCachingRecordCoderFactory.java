package com.arvatosystems.t9t.api.codecs.impl;

import java.util.HashMap;
import java.util.Map;

import de.jpaw.bonaparte.core.BonaPortable;

import com.arvatosystems.t9t.api.codecs.IMessageDecoder;
import com.arvatosystems.t9t.api.codecs.IMessageEncoder;

public class SingleThreadCachingRecordCoderFactory extends RecordCoderFactory {
    private final Map<String, IMessageDecoder<BonaPortable, byte[]>> decoders = new HashMap<>(12);
    private final Map<String, IMessageEncoder<BonaPortable, byte[]>> encoders = new HashMap<>(12);

    @Override
    public IMessageEncoder<BonaPortable, byte[]> getEncoderInstance(String mimeType) {
        IMessageEncoder<BonaPortable, byte[]> encoder = encoders.get(mimeType);
        if (encoder != null)
            return encoder;
        encoder = super.getEncoderInstance(mimeType);
        if (encoder != null)
            encoders.put(mimeType, encoder);
        return encoder;
    }

    @Override
    public IMessageDecoder<BonaPortable, byte[]> getDecoderInstance(String mimeType) {
        IMessageDecoder<BonaPortable, byte[]> decoder = decoders.get(mimeType);
        if (decoder != null)
            return decoder;
        decoder = super.getDecoderInstance(mimeType);
        if (decoder != null)
            decoders.put(mimeType, decoder);
        return decoder;
    }
}
