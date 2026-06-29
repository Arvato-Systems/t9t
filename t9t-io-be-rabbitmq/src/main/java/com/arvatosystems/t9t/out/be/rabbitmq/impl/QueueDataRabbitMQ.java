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
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Nonnull;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.RabbitMqConfiguration;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.be.async.AbstractAsyncQueueData;
import com.arvatosystems.t9t.out.services.IAsyncMessageUpdater;

public class QueueDataRabbitMQ extends AbstractAsyncQueueData {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueDataRabbitMQ.class);

    private static final int DEFAULT_CACHE_EXPIRY = 10000;
    private static final int DEFAULT_CACHE_SIZE = 10000;

    private final IAsyncMessageUpdater messageUpdater = Jdp.getRequired(IAsyncMessageUpdater.class);
    private final Channel queueChannel;

    // this map handles the publisher confirms from RabbitMQ
    // key: RabbitMQ sequenceNumber, value: objectRef of message
    private final Cache<Long, Long> outstandingConfirmsMap;

    public QueueDataRabbitMQ(@Nonnull final AsyncQueueDTO queueConfig, @Nonnull final Channel queueChannel) {
        super(queueConfig);   // always launch senders

        try {
            final RabbitMqConfiguration rabbitMqConfig = ConfigProvider.getConfiguration().getRabbitMqConfiguration();
            if (rabbitMqConfig == null) {
                throw new T9tException(T9tException.MISSING_CONFIGURATION, "Missing RabbitMQ configuration");
            }

            this.queueChannel = queueChannel;
            // init queue channel
            queueChannel.basicQos(1);
            queueChannel.queueDeclare(queueConfig.getAsyncQueueId(), true, false, false, null);

            final Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
            cacheBuilder.expireAfterWrite(rabbitMqConfig.getPublisherConfirmCacheExpiryInMs() != null ? rabbitMqConfig.getPublisherConfirmCacheExpiryInMs() : DEFAULT_CACHE_EXPIRY, TimeUnit.MILLISECONDS);
            final int publisherConfirmCacheMaxSize = rabbitMqConfig.getPublisherConfirmCacheMaxSize() != null ? rabbitMqConfig.getPublisherConfirmCacheMaxSize() : DEFAULT_CACHE_SIZE;
            cacheBuilder.maximumSize(publisherConfirmCacheMaxSize);
            if (publisherConfirmCacheMaxSize > 0) {  // 0 means cache is disabled all entries will be invalidated therefore no need to log them.
                cacheBuilder.removalListener((Long sequenceNumber, Long objectRef, RemovalCause cause) -> {
                    if (RemovalCause.EXPLICIT != cause && RemovalCause.REPLACED != cause) {
                        LOGGER.debug("SequenceNumber {} and objectRef {} for queue {} is removed from cache with cause {} before publisher confirm for received.",
                                sequenceNumber, objectRef, queueConfig.getAsyncQueueId(), cause);
                    }
                });
                // Register callback handler for publisher confirms (both ack and nack). Register only if cache size is more than 0
                queueChannel.addConfirmListener(getAckCallbackHandler(), getNackCallbackHandler());
            }
            this.outstandingConfirmsMap = cacheBuilder.build();

            // callback for queue delivery
            final DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                LOGGER.trace("Received message from queue {}: consumerTag {}", queueConfig.getAsyncQueueId(), consumerTag);
                final BonaPortable payload = new CompactByteArrayParser(delivery.getBody(), 0, -1).readRecord();
                final long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                if (!gate.get()) {
                    try {
                        sleepShallow(asyncTransmitterConfig.getTimeoutIdleRed());
                    } catch (InterruptedException e) {
                        LOGGER.error("Interrupted while waiting for REG gate of queue {}", queueConfig.getAsyncQueueId(), e);
                    }
                }
                if (sendAsync(payload)) {
                    LOGGER.trace("Message processed successfully, acknowledging deliveryTag {} for queue {}", deliveryTag, queueConfig.getAsyncQueueId());
                    queueChannel.basicAck(deliveryTag, false);
                    gate.set(true);
                } else {
                    LOGGER.error("Message processing failed, rejecting deliveryTag {} for queue {}", deliveryTag, queueConfig.getAsyncQueueId());
                    gate.set(false);
                    queueChannel.basicNack(deliveryTag, false, true);
                }
            };

            // initialize queue
            final String consumerTag = queueChannel.basicConsume(queueConfig.getAsyncQueueId(), false, deliverCallback, tag -> {
                LOGGER.warn("Consumer tag {} cancelled for queue {}", tag, queueConfig.getAsyncQueueId());
            });
            LOGGER.debug("Consumer initialized for queue {}, consumerTag {}", queueConfig.getAsyncQueueId(), consumerTag);
        } catch (Exception ex) {
            LOGGER.error("Error while launching RabbitMQ consumer for queue {}", queueConfig.getAsyncQueueId(), ex);
            throw new T9tException(T9tIOException.IO_EXCEPTION, "Unable to initiate RabbitMQ consumer for queue " + queueConfig.getAsyncQueueId(), ex);
        }
    }

    @Override
    protected void shutdown() {
        if (queueChannel != null && queueChannel.isOpen()) {
            LOGGER.debug("Closing RabbitMQ channel for queue {}", queueConfig.getAsyncQueueId());
            try {
                queueChannel.close();
            } catch (Exception e) {
                throw new T9tException(T9tIOException.IO_EXCEPTION, "Error while closing RabbitMQ channel for queue " + getQueueConfig().getAsyncQueueId(), e);
            }
        }
    }

    @Override
    protected void clearQueue() {
        try {
            if (queueChannel != null && queueChannel.isOpen()) {
                LOGGER.debug("Purging RabbitMQ queue {}", queueConfig.getAsyncQueueId());
                queueChannel.queuePurge(queueConfig.getAsyncQueueId());
            } else {
                LOGGER.error("Cannot purge RabbitMQ queue {} because the channel is not open or null", queueConfig.getAsyncQueueId());
            }
        } catch (IOException e) {
            LOGGER.error("Error while purging RabbitMQ queue {}", queueConfig.getAsyncQueueId(), e);
            throw new T9tException(T9tIOException.IO_EXCEPTION, "Unable to purge RabbitMQ queue " + queueConfig.getAsyncQueueId(), e);
        }
    }

    @Nonnull
    public Channel getQueueChannel() {
        return queueChannel;
    }

    public void storeMessageForConformation(@Nonnull Long sequenceNumber, @Nonnull Long objectRef) {
        outstandingConfirmsMap.put(sequenceNumber, objectRef);
    }

    // call back handler for ack (server response upon successfully adding message into queue)
    @Nonnull
    private ConfirmCallback getAckCallbackHandler() {
        return (sequenceNumber, multiple) -> {
            LOGGER.debug("ConfirmCallback ACK received for queue {}, sequenceNumber {}, multiple {}", queueConfig.getAsyncQueueId(), sequenceNumber, multiple);
            if (outstandingConfirmsMap.getIfPresent(sequenceNumber) != null) {
                outstandingConfirmsMap.invalidate(sequenceNumber);
            } else {
                LOGGER.warn("ConfirmCallback ACK received for queue {}, sequenceNumber {} but no matching entry found in outstandingConfirmsMap", queueConfig.getAsyncQueueId(), sequenceNumber);
            }
            if (multiple) {
                for (Long s: outstandingConfirmsMap.asMap().keySet()) {
                    if (s < sequenceNumber) {
                        outstandingConfirmsMap.invalidate(sequenceNumber);
                    }
                }
            }
        };
    }

    // call back handler for nack (server response upon failure to add message in queue)
    @Nonnull
    private ConfirmCallback getNackCallbackHandler() {
        return (sequenceNumber, multiple) -> {
            LOGGER.error("ConfirmCallback NACK received for queue {}, sequenceNumber {}, multiple {}", queueConfig.getAsyncQueueId(), sequenceNumber, multiple);
            handleNack(sequenceNumber);
            if (multiple) {
                for (Long s: outstandingConfirmsMap.asMap().keySet()) {
                    if (s < sequenceNumber) {
                        handleNack(s);
                    }
                }
            }
        };
    }

    private void handleNack(@Nonnull final Long sequenceNumber) {
        final Long objectRef = outstandingConfirmsMap.getIfPresent(sequenceNumber);
        if (objectRef != null) {
            outstandingConfirmsMap.invalidate(sequenceNumber);
            LOGGER.debug("Updating message for sequence number {} and objectRef {} to status {} for queue {}", sequenceNumber, objectRef, ExportStatusEnum.PROCESSING_ERROR, queueConfig.getAsyncQueueId());
            final AsyncHttpResponse errorResponse = new AsyncHttpResponse();
            errorResponse.setClientReference(String.valueOf(sequenceNumber));
            errorResponse.setErrorDetails("Receive NACK from RabbitMQ");
            messageUpdater.updateMessage(objectRef, ExportStatusEnum.PROCESSING_ERROR, null, null);
        } else {
            LOGGER.warn("ConfirmCallback NACK received for queue {}, sequenceNumber {} but no matching entry found in outstandingConfirmsMap", queueConfig.getAsyncQueueId(), sequenceNumber);
        }
    }
}
