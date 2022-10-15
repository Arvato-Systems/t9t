/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.vertx.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.StaticMeta;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class CompactMessageCodec implements MessageCodec<BonaPortable, BonaPortable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompactMessageCodec.class);

    public static final String COMPACT_MESSAGE_CODEC_ID = "cb";

    @Override
    public void encodeToWire(final Buffer buffer, final BonaPortable obj) {
        LOGGER.debug("Serialization of {} for sending over wire", obj.ret$PQON());
        final CompactByteArrayComposer cbac = new CompactByteArrayComposer();
        cbac.writeObject(obj);
        buffer.setBytes(0, cbac.getBuffer(), 0, cbac.getLength());
        cbac.close();
    }

    @Override
    public BonaPortable decodeFromWire(final int pos, final Buffer buffer) {
        final byte[] buff = buffer.getBytes();
        final CompactByteArrayParser cbap = new CompactByteArrayParser(buff, pos, buff.length);
        final BonaPortable obj = cbap.readObject(StaticMeta.OUTER_BONAPORTABLE, BonaPortable.class);
        LOGGER.debug("Deserialization of {} received over wire", obj.ret$PQON());
        return obj;
    }

    @Override
    public BonaPortable transform(final BonaPortable s) {
        if (s.was$Frozen())
            return s;       // immutable
        return s.ret$FrozenClone();
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
