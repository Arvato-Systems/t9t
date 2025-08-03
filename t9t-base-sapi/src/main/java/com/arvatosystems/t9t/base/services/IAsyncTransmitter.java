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
package com.arvatosystems.t9t.base.services;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * This is the application API Interface to send a message asynchronously.
 * The message will be sent after a successful commit. It will be persisted (survives a downtime).
 * The call returns a key into the async message table (or null if the implementation uses a queue).
 * The transmitted payload usually is the final external object (for example as defined in API EXT projects, and only needs to be serialized into JSON or XML).
 *
 * This is the first interface used in data flow, the one injected into the business logic methods.
 */
public interface IAsyncTransmitter {

    /**
     * Transmits a message via some asynchronous channel.
     * The method returns a reference which can be used to attempt cancelling message transmission
     * (which will only work if the implementation supports it AND the initial attempt resulted in an error / timeout).
     *
     * @param asyncChannelId the channel to which to send the message to
     * @param payload the payload
     * @param ref and optional source reference
     * @param category an optional category
     * @param identifier an optional alphanumeric identifier of the source
     * @param partition the partition in clustered environments
     * @return a unique message reference, or null in case the message was discarded because the channel was set to inactive
     */
    @Nullable
    Long transmitMessage(@Nonnull String asyncChannelId, @Nonnull BonaPortable payload,
      @Nullable Long ref, @Nullable String category, @Nullable String identifier, int partition);

    /**
     * Retransmits a message (unless its channel has been deactivated).
     *
     * @param ctx the request context
     * @param asyncChannelId the channel to which to send the message to
     * @param payload the payload
     * @param objectRef
     * @param partition the partition in clustered environments
     * @param recordKey
     */
    void retransmitMessage(RequestContext ctx, String asyncChannelId, BonaPortable payload, Long objectRef, int partition, String recordKey);

    /**
     * Cancels further retry attempts of the specified message (if supported by the implementation).
     *
     * @param messageRef the reference returned by <code>transmitMessage</code>.
     */
    void stopRetries(@Nonnull Long messageRef);
}
