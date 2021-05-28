package com.arvatosystems.t9t.base.be.stubs;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IClusterEnvironment;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
public class ClusterEnvironmentSingleNode implements IClusterEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterEnvironmentSingleNode.class);
    private final Collection<Integer> SINGLE_NODE = Collections.singletonList(Integer.valueOf(0));

    public ClusterEnvironmentSingleNode() {
        LOGGER.info("Single node setup - execution is not clustered/distributed");
    }

    @Override
    public Collection<Integer> getListOfShards(Long tenantRef) {
        return SINGLE_NODE;
    }

    @Override
    public boolean processOnThisNode(Long tenantRef, int hash) {
        // we process eveything
        return true;
    }

    @Override
    public Collection<Long> getListOfTenantRefs() {
        return Collections.emptyList();
    }

    @Override
    public int getNumberOfNodes() {
        return 1;
    }
}
