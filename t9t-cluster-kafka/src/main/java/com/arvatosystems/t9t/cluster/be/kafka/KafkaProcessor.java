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
