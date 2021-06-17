package com.arvatosystems.t9t.base.vertx.impl;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.core.StaticMeta;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class CompactMessageCodec implements MessageCodec<BonaPortable, BonaPortable> {
    public static final String COMPACT_MESSAGE_CODEC_ID = "cb";

    @Override
    public void encodeToWire(final Buffer buffer, final BonaPortable obj) {
        final CompactByteArrayComposer cbac = new CompactByteArrayComposer();
        cbac.writeObject(obj);
        buffer.setBytes(0, cbac.getBuffer(), 0, cbac.getLength());
    }

    @Override
    public BonaPortable decodeFromWire(final int pos, final Buffer buffer) {
        final byte [] buff = buffer.getBytes();
        final CompactByteArrayParser cbap = new CompactByteArrayParser(buff, pos, buff.length);
        try {
            return cbap.readObject(StaticMeta.OUTER_BONAPORTABLE, BonaPortable.class);
        } catch (final MessageParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BonaPortable transform(final BonaPortable s) {
        if (s.was$Frozen())
            return s;       // immutable
        try {
//            return s.ret$MutableClone(true, false);
            return s.ret$FrozenClone();
        } catch (final ObjectValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return COMPACT_MESSAGE_CODEC_ID;
    }

    @Override
    public byte systemCodecID() {
        return (byte)-1;
    }
}
