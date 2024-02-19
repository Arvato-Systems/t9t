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

import java.util.Collection;

import org.apache.kafka.common.TopicPartition;

import com.arvatosystems.t9t.kafka.service.impl.KafkaRebalancer;

public class KafkaClusterRebalancer extends KafkaRebalancer {

    private KafkaProcessor processingStrategy;

    public KafkaClusterRebalancer(final String topic, final boolean verbose) {
        super(topic, verbose);
    }

    @Override
    public void onPartitionsRevoked(final Collection<TopicPartition> partitions) {
        super.onPartitionsRevoked(partitions);

        // in case of shutdown: these will be all partitions
        processingStrategy.revokePartitions(partitions);
    }

    /**
     * Assign processor for handling partition revoking correctly.
     *
     * @param processingStrategy the used {@link KafkaProcessor}.
     */
    public void setProcessingStrategy(final KafkaProcessor processingStrategy) {
        this.processingStrategy = processingStrategy;
    }

    public void pause() {
        this.processingStrategy.triggerPausing();
    }

    public void resume() {
        this.processingStrategy.triggerResuming();
    }

}
