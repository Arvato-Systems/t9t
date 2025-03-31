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
