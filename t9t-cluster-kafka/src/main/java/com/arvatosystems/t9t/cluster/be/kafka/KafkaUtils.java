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
package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

public final class KafkaUtils {

    private KafkaUtils() {
        // empty, private to avoid instantiation
    }

    /**
     * Convert list of {@link Integer}s into list of {@link TopicPartition}s.
     */
    public static Collection<TopicPartition> transformToTopicPartition(final String topic, final Collection<Integer> partitions) {
        final Collection<TopicPartition> topicPartitions = new ArrayList<>(partitions.size());
        partitions.forEach(partition -> topicPartitions.add(new TopicPartition(topic, partition.intValue())));
        return topicPartitions;
    }

    /**
     * Convert list of {@link PartitionInfo}s into list of {@link TopicPartition}s. If empty, all partitions will be taken.
     */
    public static Collection<TopicPartition> transformToTopicPartition(final List<PartitionInfo> partitionInfos) {
        final Collection<TopicPartition> topicPartitions = new ArrayList<>(partitionInfos.size());
        partitionInfos.forEach(partitionInfo -> topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition())));
        return topicPartitions;
    }

}
