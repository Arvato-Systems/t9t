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
package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.kafka.service.impl.KafkaRebalancer;

public class KafkaClusterRebalancer extends KafkaRebalancer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClusterRebalancer.class);

    private Callable<Boolean> processingStrategy;

    public KafkaClusterRebalancer(String topic, boolean verbose) {
        super(topic, verbose);
    }

    @Override
    public void onPartitionsRevoked(final Collection<TopicPartition> partitions) {
        super.onPartitionsRevoked(partitions);

        // handle strategy-specific
        if (this.processingStrategy instanceof KafkaPartitionOrderedRequestProcessor processor) {
            LOGGER.info("Revoke partitions {}", Arrays.toString(partitions.toArray()));
            // in case of shutdown: these will be all partitions
            processor.revokePartitions(partitions);
        }
    }

    /**
     * Assign processor for handling partion revoking correctly.
     *
     * @param processingStrategy the used {@link Callable}
     */
    public void setProcessingStrategy(Callable<Boolean> processingStrategy) {
        this.processingStrategy = processingStrategy;
    }

}
