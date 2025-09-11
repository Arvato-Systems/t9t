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
package com.arvatosystems.t9t.kafka.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nullable;

public interface IKafkaTopicReader {

    /**
     * @return the number of partitions for the topic.
     */
    int getNumberOfPartitions();

    /**
     * @return the name of the topic.
     */
    String getKafkaTopic();

    /**
     * Polls data from topic, deserializes the data object using the compact bonaparte deserializer
     * and feeds it into the consumer.
     *
     * @return the number of records processed.
     */
    <T extends BonaPortable> int pollAndProcess(IKafkaConsumer<T> processor, Class<T> expectedType);

    /**
     * Polls data from topic, deserializes the data object using the compact bonaparte deserializer
     * and feeds it into the consumer.
     * Extended form with more control about poll interval and custom committer via lambda.
     *
     * @return the number of records processed.
     */
    <T extends BonaPortable> int pollAndProcess(IKafkaConsumer<T> processor, Class<T> expectedType, long pollIntervalInMs,
            Consumer<KafkaConsumer<String, byte[]>> committer);

    /**
     * Polls data from topic with given {@code pollIntervalInMs}.
     *
     * @param pollIntervalInMs poll interval
     *
     * @return the list of {@link ConsumerRecords}, or null if polling was interrupted
     */
    @Nullable
    ConsumerRecords<String, byte[]> poll(long pollIntervalInMs);

    /**
     * Pause consumer for given {@code partitions}.
     *
     * @param partitions to pause
     */
    void pause(Collection<TopicPartition> partitions);

    /**
     * Resume consumer for given {@code partitions}.
     *
     * @param partitions to resume
     */
    void resume(Collection<TopicPartition> partitions);

    /**
     * Provide list of partions as {@link PartitionInfo} for this consumer.
     *
     * @return {@link List} of {@link PartitionInfo}s
     */
    List<PartitionInfo> getPartitionInfos();

    /** Initiates a commit for one or multiple partitions of a topic. */
    void performPartialCommits(Map<TopicPartition, OffsetAndMetadata> partialCommits, boolean sync);

    /** Commits changes and closes the topic writer. */
    void close();

    /** Closes the consumer 'really' - see implementation. */
    void closeReally();

    /** Stop polling and signal closin */
    void wakeUp();

    /**
     * Registers the underlying {@link KafkaConsumer} for metrics.
     *
     * @param metricsAdder the {@link Consumer} hook to create MeterBinder
     */
    @SuppressWarnings("rawtypes")
    void registerMetrics(Consumer<KafkaConsumer> metricsAdder);
}
