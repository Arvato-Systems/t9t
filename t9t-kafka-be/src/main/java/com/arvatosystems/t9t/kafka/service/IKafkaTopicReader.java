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
package com.arvatosystems.t9t.kafka.service;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import de.jpaw.bonaparte.core.BonaPortable;

public interface IKafkaTopicReader {

    /**
     * Returns the number of partitions for the topic.
     */
    int getNumberOfPartitions();

    /**
     * Returns the name of the topic.
     */
    String getKafkaTopic();

    /**
     * Polls data from topic, deserializes the data object using the compact bonaparte deserializer
     * and feeds it into the consumer.
     *
     * Returns the number of records processed.
     */
    <T extends BonaPortable> int pollAndProcess(IKafkaConsumer<T> processor, Class<T> expectedType);

    /**
     * Polls data from topic, deserializes the data object using the compact bonaparte deserializer
     * and feeds it into the consumer.
     * Extended form with more control about poll interval and custom committer via lambda.
     *
     * Returns the number of records processed.
     */
    <T extends BonaPortable> int pollAndProcess(IKafkaConsumer<T> processor, Class<T> expectedType, long pollIntervalInMs,
      Consumer<KafkaConsumer<String, byte[]>> committer);

    /** Initiates a commit for one or multiple partitions of a topic. */
    void performPartialCommits(Map<TopicPartition, OffsetAndMetadata> partialCommits, boolean sync);

    /** Commits changes and closes the topic writer. */
    void close();
}
