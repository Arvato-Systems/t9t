/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
import de.jpaw.util.ByteUtil;
import de.jpaw.util.ExceptionUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class CompactMessageCodec implements MessageCodec<BonaPortable, BonaPortable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompactMessageCodec.class);
    private static final int BYTES_OF_INT = 4;  // C developers would cry for this hard coded 4, but it's from the official vert.x examples...

    public static final String COMPACT_MESSAGE_CODEC_ID = "cb";

    @Override
    public void encodeToWire(final Buffer buffer, final BonaPortable obj) {
        final CompactByteArrayComposer cbac = new CompactByteArrayComposer();
        cbac.writeObject(obj);
        buffer.appendInt(cbac.getLength());
        buffer.appendBytes(cbac.getBuffer(), 0, cbac.getLength());
        LOGGER.debug("Serialization of {} for sending over wire in COMPACT encoding as {} bytes", obj.ret$PQON(), cbac.getLength());
        cbac.close();
    }

    @Override
    public BonaPortable decodeFromWire(final int pos, final Buffer buffer) {
        final int messageLength = buffer.getInt(pos);
        LOGGER.debug("Received COMPACT message of {} bytes via eventBus with offset {}", messageLength, pos);
        final byte[] buff = buffer.getBytes(pos + BYTES_OF_INT, pos + BYTES_OF_INT + messageLength);
        try {
            final CompactByteArrayParser cbap = new CompactByteArrayParser(buff, 0, messageLength);
            final BonaPortable obj = cbap.readObject(StaticMeta.OUTER_BONAPORTABLE, BonaPortable.class);
            LOGGER.debug("Deserialization of {} received over wire", obj.ret$PQON());
            return obj;
        } catch (final Exception e) {
            LOGGER.error("Failed to decode: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
            LOGGER.debug("Buffer contents is\n{}", ByteUtil.dump(buff, Math.min(messageLength, 2048)));
            throw e;
        }
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
