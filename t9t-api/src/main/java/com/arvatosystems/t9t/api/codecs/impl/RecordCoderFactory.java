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
