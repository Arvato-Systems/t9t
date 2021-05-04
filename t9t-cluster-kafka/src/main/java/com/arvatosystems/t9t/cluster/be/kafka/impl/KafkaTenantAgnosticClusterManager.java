package com.arvatosystems.t9t.cluster.be.kafka.impl;

import java.util.Collection;
import java.util.Collections;

import com.arvatosystems.t9t.base.services.IClusterEnvironment;

import de.jpaw.dp.Singleton;

/**
 * Simple form of a cluster manager, which does not attempt to group partitions for the same tenant on the same node.
 */
@Singleton
public class KafkaTenantAgnosticClusterManager implements IClusterEnvironment {

    @Override
    public Collection<Long> getListOfTenantRefs() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Integer> getListOfShards(Long tenantRef) {
        return KafkaClusterManagerInitializer.MY_INDEXES;
    }

    @Override
    public boolean processOnThisNode(Long tenantRef, int hash) {
        if (KafkaClusterManagerInitializer.totalNumberOfPartitons <= 0) {
            return true;  // no kafka available?
        }
        final Integer partition = Integer.valueOf((hash & 0x7fffffff) % KafkaClusterManagerInitializer.totalNumberOfPartitons); 
        return KafkaClusterManagerInitializer.MY_INDEXES.contains(partition);
    }
}
