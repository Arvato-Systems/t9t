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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.api.RequestParameters;

@FunctionalInterface
public interface IInputQueuePartitioner {
    int INITIAL_PARTITION_MODULUS = 60; // 60 is a good choice because it can be divided without remainder by 1..6

    /**
     * Determines the partition key (for example hash of customer ID or SKU ID) for input data partitioning.
     * This is used to ensure that data for the same target object is always processed on the same node,
     * to ensure in order processing.
     *
     * @param some request parameters
     * @return the partition index
     */
    int determinePartitionKey(RequestParameters rq);

    /**
     * Determines a preliminary partition key, based on the key.
     *
     * The actual partition index depends on infrastructure,
     * that means the preliminary value will be computed modulus the actual number of partitions of the topic.
     *
     * @param value the partition key
     * @return the partition index
     */
    default int getPreliminaryPartitionKey(String value) {
        final int hashCode = value.hashCode();
        return (hashCode & Integer.MAX_VALUE) % INITIAL_PARTITION_MODULUS;   // ensure return value in range 0..59
    }
}
