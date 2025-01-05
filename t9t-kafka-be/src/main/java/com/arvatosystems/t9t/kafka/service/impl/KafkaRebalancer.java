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

    protected final Set<Integer> myIndexes = ConcurrentHashMap.newKeySet(60); // concurrent set
    protected final String topic;
    protected final boolean verbose;

    public KafkaRebalancer(final String topic, final boolean verbose) {
        this.topic = topic;
        this.verbose = verbose;
    }

    @Override
    public Collection<Integer> getCurrentPartitions() {
        return Collections.unmodifiableSet(myIndexes);
    }

    @Override
    public void init(final List<PartitionInfo> partitions) {
        LOGGER.info("Initially {} partitions are available for topic {}", partitions.size(), topic);
    }

    private void dumpPartitions(final String intro) {
        if (verbose) {
            LOGGER.info("{} number of shards is NOW {}", intro, myIndexes.size());
            LOGGER.info("Partitions are: {}", myIndexes.stream().map(part -> Integer.toString(part)).collect(Collectors.joining(", ")));
        }
    }

    private String partitionList(final Collection<TopicPartition> partitions) {
        final StringBuilder sb = new StringBuilder(40);
        for (final TopicPartition tp : partitions) {
            sb.append(' ').append(tp.partition());
        }
        return sb.toString();
    }

    @Override
    public void onPartitionsRevoked(final Collection<TopicPartition> partitions) {
        LOGGER.info("Rebalance! {} partitions revoked on topic {}: {}", partitions.size(), topic, partitionList(partitions));
        dumpPartitions("BEFORE");
        for (final TopicPartition tp : partitions) {
            myIndexes.remove(tp.partition());
        }
        dumpPartitions("AFTER");
    }

    @Override
    public void onPartitionsAssigned(final Collection<TopicPartition> partitions) {
        LOGGER.info("Rebalance! {} partitions assigned on topic {}: {}", partitions.size(), topic, partitionList(partitions));
        dumpPartitions("BEFORE");
        for (final TopicPartition tp : partitions) {
            myIndexes.add(tp.partition());
        }
        dumpPartitions("AFTER");
    }

}
