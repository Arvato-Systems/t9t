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

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.kafka.service.IKafkaConsumer;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.util.ExceptionUtil;

public class KafkaTopicReader implements IKafkaTopicReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicReader.class);
    public static final long DEFAULT_POLL_INTERVAL = 100L; // max wait of 1/10th of a second for input data
    public static final Integer DEFAULT_MAX_MESSAGES_PER_POLL = 24; // should be a multiple of the worker pool site for request processing [6]
    public static final Integer DEFAULT_TIMEOUT_IN_MS = 30_000; // 30 seconds

    private final String kafkaTopic; // determined via config
    private final KafkaConsumer<String, byte[]> consumer;

    public KafkaTopicReader(final String bootstrapServers, final String topic, final String groupId, final Map<String, Object> propsIn,
            final KafkaRebalancer rebalancer) {
        final Map<String, Object> props = propsIn == null ? new HashMap<>() : propsIn;
        // always take from param
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // only if not set yet
        props.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
        props.putIfAbsent(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, DEFAULT_TIMEOUT_IN_MS);
        props.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, DEFAULT_MAX_MESSAGES_PER_POLL);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());    // pass the instance instead (no reflection)
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName()); // pass the instance instead (no reflection)
        kafkaTopic = topic;
        consumer = new KafkaConsumer<>(props, new StringDeserializer(), new ByteArrayDeserializer());
        final List<PartitionInfo> partitions = consumer.partitionsFor(kafkaTopic);
        if (rebalancer == null) {
            consumer.subscribe(Collections.singleton(topic));
        } else {
            rebalancer.init(partitions);
            consumer.subscribe(Collections.singleton(topic), rebalancer);
        }
        LOGGER.info("Created reader for kafka topic {}, which has {} partitions", topic, String.valueOf(partitions.size()));
    }

    @Override
    public String getKafkaTopic() {
        return kafkaTopic;
    }

    @Override
    public int getNumberOfPartitions() {
        // due to rebalancing we need to fetch this directly from consumer
        return this.consumer.partitionsFor(kafkaTopic).size();
    }

    @Override
    public void close() {
        consumer.wakeup();
        consumer.unsubscribe();
    }

    @Override
    public void closeReally() {
        LOGGER.info("Closing consumer");
        this.consumer.close();
    }

    @Override
    public void wakeUp() {
        LOGGER.info("Calling wakeUp()");
        this.consumer.wakeup();
    }

    @Override
    public <T extends BonaPortable> int pollAndProcess(final IKafkaConsumer<T> processor, final Class<T> expectedType) {
        return pollAndProcess(processor, expectedType, DEFAULT_POLL_INTERVAL, x -> x.commitAsync());
    }

    @Override
    public <T extends BonaPortable> int pollAndProcess(final IKafkaConsumer<T> processor, final Class<T> expectedType, final long pollIntervalInMs,
            final Consumer<KafkaConsumer<String, byte[]>> committer) {
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
            for (final ConsumerRecord<String, byte[]> oneRecord : records) {
                final BonaPortable obj;
                try {
                    obj = new CompactByteArrayParser(oneRecord.value(), 0, -1).readRecord();
                } catch (Exception e) {
                    LOGGER.error("Data in topic {} for key {} is not a parseable BonaPortable: {}", kafkaTopic, oneRecord.key(), e.getMessage());
                    continue;
                }
                if (obj == null || !expectedType.isAssignableFrom(obj.getClass())) {
                    LOGGER.error("Data in topic {} for key {} is not the expected type {}, but {}", kafkaTopic, oneRecord.key(),
                            expectedType.getCanonicalName(), obj == null ? "NULL" : obj.getClass().getCanonicalName());
                    continue;
                }
                final T typedObj = (T) obj;
                try {
                    processor.accept(typedObj, oneRecord.partition(), oneRecord.offset(), oneRecord.key());
                } catch (Exception e) {
                    LOGGER.error("Uncaught exception processing data of topic {} for key {}: {}", kafkaTopic, oneRecord.key(), e.getMessage());
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

    @Override
    public void performPartialCommits(final Map<TopicPartition, OffsetAndMetadata> offsets, final boolean sync) {
        if (sync) {
            consumer.commitSync(offsets);
        } else {
            consumer.commitAsync(offsets, (o, e) -> {
                if (e != null) {
                    LOGGER.error("Partial commit failed: {}", ExceptionUtil.causeChain(e));
                }
            });
        }
    }

    @Override
    public ConsumerRecords<String, byte[]> poll(final long pollIntervalInMs) {
        try {
            return consumer.poll(Duration.ofMillis(pollIntervalInMs));
        } catch (final WakeupException exc) {
            LOGGER.info("Polling stopped by wakeUp(): {}", exc.getMessage());
        } catch (final Exception exc) {
            LOGGER.info("Polling interrupted by unexpected error {} with message: {}", exc.getClass().getSimpleName(), exc.getMessage());
        }
        return null;
    }

    @Override
    public void pause(final Collection<TopicPartition> partitionsToPause) {
        this.consumer.pause(partitionsToPause);
    }

    @Override
    public void resume(Collection<TopicPartition> partitionsToResume) {
        this.consumer.resume(partitionsToResume);
    }

    @Override
    public List<PartitionInfo> getPartitionInfos() {
        // due to rebalancing we need to fetch this directly from consumer
        return this.consumer.partitionsFor(kafkaTopic);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void registerMetrics(final Consumer<KafkaConsumer> metricsAdder) {
        metricsAdder.accept(consumer);
    }

}
