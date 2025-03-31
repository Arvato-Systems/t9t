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
package com.arvatosystems.t9t.base.be.stubs;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.services.IClusterEnvironment;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
public class ClusterEnvironmentSingleNode implements IClusterEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterEnvironmentSingleNode.class);
    private static final Collection<Integer> SINGLE_NODE = Collections.singletonList(Integer.valueOf(0));

    public ClusterEnvironmentSingleNode() {
        LOGGER.info("Single node setup - execution is not clustered/distributed");
    }

    @Override
    public Collection<Integer> getListOfShards(final String tenantId) {
        return SINGLE_NODE;
    }

    @Override
    public boolean processOnThisNode(final String tenantId, final int hash) {
        // we process everything
        return true;
    }

    @Override
    public Collection<String> getListOfTenantIds() {
        return Collections.emptyList();
    }

    @Override
    public int getNumberOfNodes() {
        return 1;
    }

    @Override
    public void processOnOtherNode(final ServiceRequest srq, final int targetPartition, final Object recordKey) {
        LOGGER.error("Logic error: Cannot transfer request {} to different node {} in single node environment for key {}!",
          srq.getRequestParameters().ret$PQON(), targetPartition, recordKey);
        throw new T9tException(T9tException.INVALID_PROCESSING);
    }
}
