package com.arvatosystems.t9t.kafka.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.kafka.service.IKafkaRebalancer;

public class KafkaRebalancer implements IKafkaRebalancer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRebalancer.class);
    private final Set<Integer> myIndexes = ConcurrentHashMap.newKeySet(96);  // concurrent set
    private final String topic;
    private final boolean verbose;

    public KafkaRebalancer(final String topic, final boolean verbose) {
        this.topic = topic;
        this.verbose = verbose;
    }

    public Collection<Integer> getCurrentPartitions() {
        return Collections.unmodifiableSet(myIndexes);
    }

    public void init(final List<PartitionInfo> partitions) {
        LOGGER.info("Initially {} partitions have been assigned", partitions.size());
        for (final PartitionInfo pi: partitions) {
            myIndexes.add(pi.partition());
        }
        dumpPartitions("INITIAL");
    }

    private void dumpPartitions(final String intro) {
        if (verbose) {
            LOGGER.info("{} number of shards is NOW {}", intro, myIndexes.size());
            LOGGER.info("Partitions are: {}", myIndexes.stream().map(part -> Integer.toString(part)).collect(Collectors.joining(", ")));
        }
    }

    @Override
    public void onPartitionsRevoked(final Collection<TopicPartition> partitions) {
        LOGGER.info("Rebalance! {} partitions revoked on topic {}", partitions.size(), topic);
        dumpPartitions("BEFORE");
        for (TopicPartition tp: partitions) {
            myIndexes.remove(tp.partition());
        }
        dumpPartitions("AFTER");
    }

    @Override
    public void onPartitionsAssigned(final Collection<TopicPartition> partitions) {
        LOGGER.info("Rebalance! {} partitions assigned on topic {}", partitions.size(), topic);
        dumpPartitions("BEFORE");
        for (TopicPartition tp: partitions) {
            myIndexes.add(tp.partition());
        }
        dumpPartitions("AFTER");
    }
}
