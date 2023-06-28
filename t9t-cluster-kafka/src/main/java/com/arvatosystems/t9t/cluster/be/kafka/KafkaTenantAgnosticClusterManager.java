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
import java.util.Collections;

import com.arvatosystems.t9t.base.services.IClusterEnvironment;

import de.jpaw.dp.Singleton;

/**
 * Simple form of a cluster manager, which does not attempt to group partitions for the same tenant on the same node.
 * A cluster manager for multiple smaller tenants would store a partition offset and modulus within the tenant.
 */
@Singleton
public class KafkaTenantAgnosticClusterManager implements IClusterEnvironment {

    @Override
    public Collection<String> getListOfTenantIds() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Integer> getListOfShards(final String tenantId) {
        return KafkaRequestProcessorAndClusterManagerInitializer.getRebalancer().getCurrentPartitions();
    }

    @Override
    public boolean processOnThisNode(final String tenantId, final int hash) {
        return KafkaRequestProcessorAndClusterManagerInitializer.processOnThisNode(tenantId, hash);
    }

    @Override
    public int getNumberOfNodes() {
        return KafkaRequestProcessorAndClusterManagerInitializer.getNumberOfPartitons();
    }
}
