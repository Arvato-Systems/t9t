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
package com.arvatosystems.t9t.msglog.kafka;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.cfg.be.LogWriterConfiguration;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicWriter;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicWriter;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.services.IMsglogPersistenceAccess;

import de.jpaw.dp.Singleton;

@Singleton
public class RequestLoggerToKafka implements IMsglogPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggerToKafka.class);

    private final KafkaConfiguration kafkaConfig = ConfigProvider.getConfiguration().getKafkaConfiguration();
    private final LogWriterConfiguration logWriterConfig = ConfigProvider.getConfiguration().getLogWriterConfiguration();
    private final String defaultBootstrapServers = kafkaConfig == null ? null : kafkaConfig.getDefaultBootstrapServers();
    private final String serverType = logWriterConfig == null ? null : logWriterConfig.getServerType();
    private final String logTopic = logWriterConfig == null ? null : logWriterConfig.getKafkaTopic();

    @IsLogicallyFinal
    private IKafkaTopicWriter kafkaWriter = null;

    @Override
    public void open() {
        if (logTopic == null || defaultBootstrapServers == null) {
            LOGGER.warn("No log writer topic configured in server.xml logWriterConfig, or no bootstrap servers in kafka section, cannot send logs!");
        } else {
            LOGGER.info("Msglog to kafka topic {} / bootstrap servers {}", logTopic, defaultBootstrapServers);
            try {
                kafkaWriter = new KafkaTopicWriter(defaultBootstrapServers, logTopic, new Properties());
            } catch (final Exception e) {
                LOGGER.error("Could not initialize Msglog to kafka topic {} / bootstrap servers {}: {}", logTopic, defaultBootstrapServers, e);
            }
        }
    }

    @Override
    public void write(final List<MessageDTO> entries) {
        if (kafkaWriter != null) {
            for (final MessageDTO m: entries) {
                m.setServerType(serverType);
                kafkaWriter.write(m, 0, null);
            }
        }
    }

    @Override
    public void close() {
        if (kafkaWriter != null) {
            kafkaWriter.close();
        }
    }
}
