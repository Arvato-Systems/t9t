package com.arvatosystems.t9t.out.jpa.impl;

import java.util.UUID;

import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.out.services.IAsyncIdempotencyHeader;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Singleton;

@Singleton
public class AsyncIdempotencyHeader implements IAsyncIdempotencyHeader {
    private static final long UUID_HIGH = 0x8e03978e40d543e8L;

    @Override
    public String getIdempotencyHeaderVariable(final AsyncChannelDTO channel, final Long messageRef, final BonaPortable payload) {
        if (channel.getIdempotencyHeaderType() == null) {
            return null;
        }
        switch (channel.getIdempotencyHeaderType()) {
        case CUSTOM:
            break;
        case MESSAGE_REFERENCE:
            return '"' + Long.toHexString(messageRef) + '"';
        case NONE:
            return null;
        case UUID:
            return '"' + (new UUID(UUID_HIGH, messageRef).toString()) + '"';
        default:
            break;
        }
        return null;
    }
}
