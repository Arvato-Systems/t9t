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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.request.QueueStatus;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * Defines methods for an async queue implementation.
 * This interface is an intermediate step in the data flow, it directs to a selected implementation of the internal queuing (JMS, KAFKA, LTQ or noop).
 *
 * Business logic usually does not see this interface.
 *  */
public interface IAsyncQueue {
    /** Queues a message (initial send). */
    void sendAsync(RequestContext ctx, AsyncChannelDTO asyncChannel, BonaPortable payload, Long objectRef, int partition, String recordKey);

    /** Initializes all queues. */
    default void open() { }

    /** Closes all queues. */
    default void close() { }

    /** Closes a specific queue (shuts it down). */
    default void close(final Long queueRef) { }

    /** Clear all queues (queueRef = null) or a specific one. */
    default void clearQueue(final Long queueRef) { }  // removes any items from the queue, required after removing dead items

    /** Opens a specific new queue, if it did not exist before. */
    default void open(final AsyncQueueDTO queue) { }

    /** returns the queue status for one or all queues. Only for queues supporting it. */
    default QueueStatus getQueueStatus(final Long queueRef, final String queueId) {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED);
    }

    /** Determines if the message should be persisted in the DB or if the implementation does it itself. */
    default boolean persistInDb() {
        return true;
    }
}
