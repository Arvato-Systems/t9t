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
package com.arvatosystems.t9t.kafka.service.impl;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.IKafkaRequestTransmitter;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicWriter;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.dp.Singleton;
import de.jpaw.json.JsonParser;
import de.jpaw.util.ConfigurationReaderFactory;

@Singleton
public class KafkaRequestTransmitter implements IKafkaRequestTransmitter {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRequestTransmitter.class);

    public static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.kafka", null);

    private static final String KAFKA_DEFAULT_BOOTSTRAP_KEY  = "t9t.kafka.bootstrap.servers";
    private static final String KAFKA_DEFAULT_TOPIC_KEY      = "t9t.kafka.request.topic";
    private static final String KAFKA_DEFAULT_PROPERTIES_KEY = "t9t.kafka.request.properties";

    @IsLogicallyFinal
    private IKafkaTopicWriter topicWriter = null;
    private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);

    /** Default no-args constructor, for injection, used by API gateways. */
    public KafkaRequestTransmitter() {
        this(CONFIG_READER.getProperty(KAFKA_DEFAULT_TOPIC_KEY, T9tConstants.DEFAULT_KAFKA_TOPIC_SINGLE_TENANT_REQUESTS));
    }

    /** Constructor for separate topics (requests to other server types), used for server-to-server communication. */
    public KafkaRequestTransmitter(final String kafkaTopic) {
        final String kafkaBootstrapServers = CONFIG_READER.getProperty(KAFKA_DEFAULT_BOOTSTRAP_KEY, null);
        if (kafkaBootstrapServers == null) {
            LOGGER.error("No configuration found for t9t.kafka.bootstrap.servers, refusing to initialize (set to /dev/null in order to discard messages)");
            // throw new T9tException(T9tException.MISSING_KAFKA_BOOTSTRAP);  // later, currently just warn
        } else {
            LOGGER.info("Kafka topic {} initializing, bootstrap servers have configured to be <{}>",
                    kafkaTopic, kafkaBootstrapServers);
            if (kafkaBootstrapServers.equals("/dev/null")) {
                // create a dummy config
                LOGGER.warn("kafka request queue set to /dev/null - discarding messages");
            } else {
                try {
                    final Properties props = new Properties();
                    props.put(ProducerConfig.LINGER_MS_CONFIG, 100);
                    props.put(ProducerConfig.RETRIES_CONFIG, 2);
                    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 8000);  // approx 5 orders
                    // props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getDataSinkId());

                    // read extra properties from environment or system variable, and parse it as arbitrary JSON
                    final String additionalProperties = CONFIG_READER.getProperty(KAFKA_DEFAULT_PROPERTIES_KEY, null);
                    if (additionalProperties != null) {
                        try {
                            final JsonParser p = new JsonParser(additionalProperties, true);
                            final Map<String, Object> extraProps = p.parseObject();
                            LOGGER.info("Found {} additional Producer configuration properties for kafka in config {}",
                                    extraProps.size(), KAFKA_DEFAULT_PROPERTIES_KEY);
                            for (Map.Entry<String, Object> entry: extraProps.entrySet()) {
                                props.put(entry.getKey(), entry.getValue());
                            }
                        } catch (final Exception e) {
                            LOGGER.error("Could not parse extra properties {} (set to {}", KAFKA_DEFAULT_PROPERTIES_KEY, additionalProperties);
                            LOGGER.error("Ignoring those:", e);
                        }
                    }
                    topicWriter = new KafkaTopicWriter(kafkaBootstrapServers, kafkaTopic, props);
                } catch (final Exception e) {
                    LOGGER.error("FATAL: Could not connect to kafka bootstrap servers - messages will be discarded", e);
                    // decide about "throw e", respectively "not catching" in order to have servers fail at startup when no kafka is available
                }
            }
        }
    }

    @Override
    public void write(ServiceRequest srq, int partition, Object recordKey) {
        write(srq, partition, recordKey == null ? null : recordKey.toString() + ":" + partition);
    }

    @Override
    public void write(final ServiceRequest srq, final String partitionKey, final Object recordKey) {
        write(srq, partitionKey.hashCode(), recordKey == null ? null : recordKey.toString() + ":" + partitionKey);
    }

    protected void write(final ServiceRequest srq, final int partition, final String recordKey) {
        if (!initialized()) {
            // redirect to /dev/null (but complain)
            LOGGER.warn("write called but no topic writer initialized");
            return;
        }
        if (shutdownInProgress.get()) {
            LOGGER.error("Shutdown in progress! Rejecting message to partition {}, recordKey {} for {}",
                partition, recordKey, srq.getRequestParameters().ret$PQON());
            throw new T9tException(T9tException.SHUTDOWN_IN_PROGRESS);
        }
        topicWriter.write(srq, partition, recordKey);
    }

    @Override
    public boolean initialized() {
        return topicWriter != null;
    }

    @Override
    public void initiateShutdown() {
        shutdownInProgress.set(true);  // reject any further request
        if (initialized()) {
            topicWriter.flush();       // flush previously written pending messages
        }
    }

    @Override
    public void shutdown() {
        if (initialized()) {
            topicWriter.close();
        }
    }
}
