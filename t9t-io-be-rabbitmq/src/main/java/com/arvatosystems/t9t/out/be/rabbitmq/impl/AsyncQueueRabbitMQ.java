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
package com.arvatosystems.t9t.out.be.rabbitmq.impl;

import java.io.IOException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.be.async.AbstractAsyncQueueData;
import com.arvatosystems.t9t.out.be.async.AbstractAsyncQueueService;
import com.arvatosystems.t9t.out.be.rabbitmq.IQueueConnectionRabbitMQ;
import com.arvatosystems.t9t.out.services.IAsyncQueue;

@Singleton
@Named("RABBITMQ")
public class AsyncQueueRabbitMQ extends AbstractAsyncQueueService implements IAsyncQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncQueueRabbitMQ.class);

    private final IQueueConnectionRabbitMQ connectionService = Jdp.getRequired(IQueueConnectionRabbitMQ.class);

    @IsLogicallyFinal
    private Connection queueConnection = null;

    // called from constructor
    @Nonnull
    @Override
    protected QueueDataRabbitMQ createQueueData(@Nonnull AsyncQueueDTO queueConfig) {
        try {
            final IQueueConnectionRabbitMQ connService = Jdp.getRequired(IQueueConnectionRabbitMQ.class);
            if (queueConnection == null || !queueConnection.isOpen()) {
                queueConnection = connService.getQueueConnection();
            }
            final Channel queueChannel = connService.getQueueChannel(queueConnection, queueConfig);
            return new QueueDataRabbitMQ(queueConfig, queueChannel);
        } catch (Exception ex) {
            LOGGER.error("Error while creating RabbitMQ channel for queue {}", queueConfig.getAsyncQueueId(), ex);
            throw new T9tException(T9tIOException.IO_EXCEPTION, "Unable to create RabbitMQ channel for queue " + queueConfig.getAsyncQueueId(), ex);
        }
    }

    @Override
    public void sendAsync(final RequestContext ctx, final AsyncChannelDTO asyncChannel, final BonaPortable payload, final Long objectRef, final int partition, final String recordKey) {
        if (asyncChannel.getAsyncQueueRef() == null) {
            LOGGER.error("Channel {} has no queue reference, cannot send message {}, with objectRef {}", asyncChannel.getAsyncChannelId(), payload, objectRef);
            return;
        }
        final QueueDataRabbitMQ queueData = (QueueDataRabbitMQ) queueDataMap.get(asyncChannel.getAsyncQueueRef().getObjectRef());
        if (queueData != null) {
            final AsyncQueueDTO queueConfig = queueData.getQueueConfig();
            try {
                final InMemoryMessage message = getMessage(ctx.tenantId, asyncChannel.getAsyncChannelId(), objectRef, payload);
                message.freeze();
                final byte[] data = getBytes(message);
                final Channel queueChannel = getQueueChannel(queueData);
                final Long sequenceNumber = queueChannel.getNextPublishSeqNo();
                queueData.storeMessageForConformation(sequenceNumber, objectRef);
                queueChannel.basicPublish("", queueConfig.getAsyncQueueId(), MessageProperties.PERSISTENT_TEXT_PLAIN, data);
                LOGGER.debug("Message sent to queue {}, objectRef {}, sequenceNumber {}", queueConfig.getAsyncQueueId(), objectRef, sequenceNumber);
            } catch (Exception ex) {
                LOGGER.error("Error while sending message to RabbitMQ queue {}, objectRef: {}", queueConfig.getAsyncQueueId(), objectRef, ex);
                throw new T9tException(T9tIOException.IO_EXCEPTION, "Failed to send message to RabbitMQ queue " + queueConfig.getAsyncQueueId() + ", objectRef: " + objectRef, ex);
            }
        }

    }

    /** For the external queue implementation, usually the messages are not persisted, but this is configurable. */
    @Override
    public boolean persistInDb() {
        return asyncTransmitterConfig == null || !T9tUtil.isTrue(asyncTransmitterConfig.getDoNotPersistMessages());
    }

    @Override
    public void close() {
        super.close();
        // all queues are closed now close the connection
        if (queueConnection != null) {
            try {
                queueConnection.close();
            } catch (IOException e) {
                throw new T9tException(T9tIOException.IO_EXCEPTION, "Failed to close RabbitMQ connection", e);
            }
        }
    }

    @Nonnull
    private byte[] getBytes(@Nonnull final InMemoryMessage message) {
        try (CompactByteArrayComposer cbac = new CompactByteArrayComposer(false);) {
            cbac.reset();
            cbac.writeRecord(message);
            return cbac.getBytes();
        }
    }

    @Nonnull
    private Channel getQueueChannel(@Nonnull final QueueDataRabbitMQ queueData) throws Exception {
        if (queueData.getQueueChannel().isOpen()) {
            return queueData.getQueueChannel();
        } else {
            if (queueConnection == null || !queueConnection.isOpen()) {
                queueConnection = connectionService.getQueueConnection();
            }
            return connectionService.getQueueChannel(queueConnection, queueData.getQueueConfig());
        }
    }

    @Nullable
    @Override
    protected Boolean isQueueGreen(@Nonnull final AbstractAsyncQueueData queueData) {
        return queueData.isGateGreen();
    }
}
