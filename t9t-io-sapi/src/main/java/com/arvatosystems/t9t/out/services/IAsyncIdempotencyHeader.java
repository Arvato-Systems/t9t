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
