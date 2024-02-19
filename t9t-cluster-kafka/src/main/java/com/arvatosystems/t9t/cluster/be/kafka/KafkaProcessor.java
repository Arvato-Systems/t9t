package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.kafka.common.TopicPartition;

/**
 * Kafka processor interface used by {@link KafkaRequestProcessorAndClusterManagerInitializer} to switch between different kafka processing strategies.
 */
public interface KafkaProcessor extends Callable<Boolean> {

    void revokePartitions(Collection<TopicPartition> partitions);

    /**
     * Pause all Kafka partitions.
     */
    void triggerPausing();

    /**
     * Resume all Kafka partitions.
     */
    void triggerResuming();

}
