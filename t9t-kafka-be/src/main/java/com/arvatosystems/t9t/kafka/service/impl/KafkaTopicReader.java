/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayParser;

public class KafkaTopicReader implements IKafkaTopicReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicReader.class);
    public static final long DEFAULT_POLL_INTERVAL = 250L;  // poll 4 times a second

    private final String kafkaTopic;              // determined via config
    private final KafkaConsumer<String, byte[]> consumer;
    private final List<PartitionInfo> partitions;
    private final int numberOfPartitions;

    public KafkaTopicReader(final String bootstrapServers, final String topic, final String groupId, final Map<String, Object> propsIn,
      final KafkaRebalancer rebalancer) {
        final Map<String, Object> props = propsIn == null ? new HashMap<>() : propsIn;
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,                 groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,       Boolean.FALSE);  // or "false" as found in examples?
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG,       60_000);  // 1 minute
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,         32);      // max number of records returned by poll() (do not overload the system)
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());    // pass the instance instead (no reflection)
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName()); // pass the instance instead (no reflection)
        kafkaTopic = topic;
        consumer = new KafkaConsumer<>(props, new StringDeserializer(), new ByteArrayDeserializer());
        partitions = consumer.partitionsFor(kafkaTopic);
        numberOfPartitions = partitions.size();
        if (rebalancer == null) {
            consumer.subscribe(Collections.singleton(topic));
        } else {
            rebalancer.init(partitions);
            consumer.subscribe(Collections.singleton(topic), rebalancer);
        }
        LOGGER.info("Created reader for kafka topic {}, which has {} partitions", topic, numberOfPartitions);
    }

    @Override
    public String getKafkaTopic() {
        return kafkaTopic;
    }

    @Override
    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    @Override
    public void close() {
        consumer.wakeup();
        consumer.unsubscribe();
    }

    @Override
    public <T extends BonaPortable> int pollAndProcess(final BiConsumer<String, T> processor, final Class<T> expectedType) {
        return pollAndProcess(processor, expectedType, DEFAULT_POLL_INTERVAL, x -> x.commitAsync());
    }

    @Override
    public <T extends BonaPortable> int pollAndProcess(final BiConsumer<String, T> processor, final Class<T> expectedType,
      final long pollIntervalInMs, final Consumer<KafkaConsumer<String, byte[]>> committer) {
        final ConsumerRecords<String, byte[]> records;
        try {
            records = consumer.poll(Duration.ofMillis(pollIntervalInMs));
        } catch (Exception e) {
            // could have been caused by interupted poll via wakup due to consumer close - inform, but return 0
            LOGGER.warn("polling failed: {}: {}", e.getClass().getSimpleName(), e.getMessage());
            return -1;
        }
        final int count = records.count();
        if (count > 0) {
            LOGGER.debug("Received {} data records via kafka", count);
            for (final ConsumerRecord<String, byte[]> record : records) {
                final BonaPortable obj;
                try {
                    obj = new CompactByteArrayParser(record.value(), 0, -1).readRecord();
                } catch (Exception e) {
                    LOGGER.error("Data in topic {} for key {} is not a parseable BonaPortable", kafkaTopic, record.key(), e.getMessage());
                    continue;
                }
                if (obj == null || !expectedType.isAssignableFrom(obj.getClass())) {
                    LOGGER.error("Data in topic {} for key {} is not the expected type {}, but {}", kafkaTopic, record.key(),
                      expectedType.getCanonicalName(), obj == null ? "NULL" : obj.getClass().getCanonicalName());
                    continue;
                }
                final T typedObj = (T)obj;
                try {
                    processor.accept(record.key(), typedObj);
                } catch (Exception e) {
                    LOGGER.error("Uncaught exception processing data iof topic {} for key {}: {}", kafkaTopic, record.key(), e.getMessage());
                    continue;
                }
            }
            // if committing should be done after processing the batch (instead of per record), invoke it now
            if (committer != null) {
                committer.accept(consumer);
            }
        }
        return count;
    }
}
