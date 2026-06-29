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

import javax.annotation.Nonnull;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.RabbitMqConfiguration;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.out.be.rabbitmq.IQueueConnectionRabbitMQ;

@Singleton
public class QueueConnectionRabbitMQ implements IQueueConnectionRabbitMQ {

    @Override
    @Nonnull
    public Connection getQueueConnection() throws Exception {
        final RabbitMqConfiguration rabbitMqConfig = ConfigProvider.getConfiguration().getRabbitMqConfiguration();
        if (rabbitMqConfig == null) {
            throw new T9tException(T9tException.MISSING_CONFIGURATION, "Missing RabbitMQ configuration");
        }
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqConfig.getHost());
        if (rabbitMqConfig.getPort() != null) {
            factory.setPort(rabbitMqConfig.getPort());
        }
        if (rabbitMqConfig.getUsername() != null) {
            factory.setUsername(rabbitMqConfig.getUsername());
        }
        if (rabbitMqConfig.getPassword() != null) {
            factory.setPassword(rabbitMqConfig.getPassword());
        }
        if (rabbitMqConfig.getConnectionTimeoutInMs() != null) {
            factory.setConnectionTimeout(rabbitMqConfig.getConnectionTimeoutInMs());
        }
        if (rabbitMqConfig.getRequestedHeartbeatInSec() != null) {
            factory.setRequestedHeartbeat(rabbitMqConfig.getRequestedHeartbeatInSec());
        }
        if (rabbitMqConfig.getNetworkRecoveryIntervalInMs() != null) {
            factory.setNetworkRecoveryInterval(rabbitMqConfig.getNetworkRecoveryIntervalInMs());
        }
        if (rabbitMqConfig.getAutomaticRecoveryEnabled() != null) {
            factory.setAutomaticRecoveryEnabled(rabbitMqConfig.getAutomaticRecoveryEnabled());
        }
        return factory.newConnection();
    }

    @Override
    @Nonnull
    public Channel getQueueChannel(@Nonnull final Connection queueConnection, @Nonnull final AsyncQueueDTO queueConfig) throws Exception {
        final Channel queueChannel = queueConnection.createChannel();
        queueChannel.queueDeclare(queueConfig.getAsyncQueueId(), true, false, false, null);
        queueChannel.confirmSelect();
        return queueChannel;
    }
}
