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
