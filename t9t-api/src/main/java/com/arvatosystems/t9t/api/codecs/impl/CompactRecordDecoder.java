package com.arvatosystems.t9t.api.codecs.impl;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

import com.arvatosystems.t9t.api.codecs.IMessageDecoder;

public class CompactRecordDecoder<O extends BonaPortable> implements IMessageDecoder<O, byte[]> {

    @Override
    public O decode(byte[] data, ObjectReference di) throws MessageParserException {
        final CompactByteArrayParser cbap = new CompactByteArrayParser(data, 0, -1);
        return (O) cbap.readRecord();
    }
}
