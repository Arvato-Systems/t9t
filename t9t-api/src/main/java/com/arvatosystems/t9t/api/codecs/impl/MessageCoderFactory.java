package com.arvatosystems.t9t.api.codecs.impl;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;

import com.arvatosystems.t9t.api.codecs.IMessageCoderFactory;
import com.arvatosystems.t9t.api.codecs.IMessageDecoder;
import com.arvatosystems.t9t.api.codecs.IMessageEncoder;

public class MessageCoderFactory<D extends BonaPortable, E extends BonaPortable> implements IMessageCoderFactory<D, E, byte[]> {

    private final Class<D> decoderClass;

    public MessageCoderFactory(Class<D> decoderClass, Class<E> encoderClass) {
        this.decoderClass = decoderClass;
    }

    @Override
    public IMessageEncoder<E, byte[]> getEncoderInstance(String mimeType) {
        if (mimeType.equals(MimeTypes.MIME_TYPE_BONAPARTE))
            return new BonaparteEncoder<E>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE))
            return new CompactBonaparteEncoder<E>();
        if (mimeType.equals(MimeTypes.MIME_TYPE_JSON))
            return new JsonEncoder<E>();
        return null;
    }

    @Override
    public IMessageDecoder<D, byte[]> getDecoderInstance(String mimeType) {
        if (mimeType.equals(MimeTypes.MIME_TYPE_BONAPARTE))
            return new BonaparteDecoder<D>(decoderClass);
        if (mimeType.equals(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE))
            return new CompactBonaparteDecoder<D>(decoderClass);
        if (mimeType.equals(MimeTypes.MIME_TYPE_JSON))
            return new JsonDecoder<D>(decoderClass);
        return null;
    }
}
