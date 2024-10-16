/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.io.Closeable;

import com.arvatosystems.t9t.out.services.IAsyncTransmitter;
import com.arvatosystems.t9t.out.services.IGenericRemoter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Implements to sync of stock values to a remote system.
 */
public class GenericRemoterViaAsyncTransmitter implements IGenericRemoter, Closeable {
    private final IAsyncTransmitter asyncTransmitter = Jdp.getRequired(IAsyncTransmitter.class);
    private final String asyncChannelId;
    private final String category;
    private final String identifier;
    private final int partitionCount;
    private int counter = 0;

    /**
     * Creates a new transmitter, using the t9t async system.
     *
     * @param asyncChannelId   the key for the channel configuration
     * @param category         a constant which determines the category
     * @param identifier       an optional more detailed key for the messages
     * @param partitionCount   number of partitions. In case of doubt, pass T9tConstants.DEFAULT_KAFKA_PARTITION_COUNT_SMALL (12)
     */
    public GenericRemoterViaAsyncTransmitter(@Nonnull final String asyncChannelId, @Nonnull final String category, @Nullable final String identifier,
            final int partitionCount) {
        this.asyncChannelId = asyncChannelId;
        this.category = category;
        this.identifier = identifier;
        this.partitionCount = partitionCount;
    }

    @Override
    public void send(final BonaPortable exportData) {
        asyncTransmitter.transmitMessage(asyncChannelId, exportData, (long)counter, category, identifier, counter % partitionCount);
        ++counter;
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public void close() {
    }

    @Override
    public String getImplementation() {
        return "async";
    }
}
