package com.arvatosystems.t9t.out.services;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.io.AsyncChannelDTO;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Provides methods to determine an idempotency header. */
public interface IAsyncIdempotencyHeader {
    @Nonnull
    default String getIdempotencyHeader(@Nonnull final AsyncChannelDTO channel) {
        return T9tUtil.nvl(channel.getIdempotencyHeader(), T9tConstants.HTTP_HEADER_IDEMPOTENCY_KEY);
    }

    @Nullable
    String getIdempotencyHeaderVariable(@Nonnull AsyncChannelDTO channel, @Nonnull Long messageRef, @Nonnull BonaPortable payload);
}
