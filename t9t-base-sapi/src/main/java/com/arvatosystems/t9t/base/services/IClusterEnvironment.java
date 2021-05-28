package com.arvatosystems.t9t.base.services;

import java.util.Collection;

/**
 * Services to decide whether data of a given shard should be processed here.
 *
 */
public interface IClusterEnvironment {
    /** Returns the current number of active nodes in the cluster. */
    int getNumberOfNodes();

    /** Returns a collection of tenantRefs which are processed by this node, or an empty list if this node processes data for all of them. */
    Collection<Long> getListOfTenantRefs();

    /** Returns a collection of shards [0...numberOfNodes-1] processed by this node. */
    Collection<Integer> getListOfShards(Long tenantRef);

    /** Decides if a given index should be processed on this shard (preferred API). */
    boolean processOnThisNode(Long tenantRef, int hash);
}
