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
package com.arvatosystems.t9t.base.services;

import java.util.Collection;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceRequest;

/**
 * Services to decide whether data of a given shard should be processed here.
 *
 */
public interface IClusterEnvironment {
    /** Returns the current number of active nodes in the cluster. */
    int getNumberOfNodes();

    /** Returns a collection of tenantIds which are processed by this node, or an empty list if this node processes data for all of them. */
    Collection<String> getListOfTenantIds();

    /** Returns a collection of shards [0...numberOfNodes-1] processed by this node. */
    Collection<Integer> getListOfShards(String tenantId);

    /** Decides if a given index should be processed on this shard (preferred API). */
    boolean processOnThisNode(String tenantId, int hash);

    /** Ships a request to some other node for processing. */
    void processOnOtherNode(ServiceRequest srq, int targetPartition, Object recordKey);

    /** Pause partitions for underlying queuing implementation like Kafka. */
    default void pausePartitions() {
        throw new T9tException(T9tException.UNSUPPORTED_OPERATION);
    }

    /** Resume partitions for underlying queuing implementation like Kafka. */
    default void resumePartitions() {
        throw new T9tException(T9tException.UNSUPPORTED_OPERATION);
    }
}
