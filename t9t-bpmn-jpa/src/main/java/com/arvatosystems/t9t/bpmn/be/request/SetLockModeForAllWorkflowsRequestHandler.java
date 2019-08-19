/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn.be.request;

import javax.persistence.Query;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessDefinitionEntityResolver;
import com.arvatosystems.t9t.bpmn.request.SetLockModeForAllWorkflowsRequest;

import de.jpaw.dp.Jdp;

public class SetLockModeForAllWorkflowsRequestHandler extends AbstractRequestHandler<SetLockModeForAllWorkflowsRequest> {
    protected final IProcessDefinitionEntityResolver resolver = Jdp.getRequired(IProcessDefinitionEntityResolver.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(final SetLockModeForAllWorkflowsRequest request) throws Exception {
        Query q = resolver.getEntityManager().createQuery(
                "UPDATE " + resolver.getBaseJpaEntityClass().getSimpleName() + " SET useExclusiveLock = :lockMode WHERE tenantRef = :tenantRef");
        q.setParameter("lockMode", request.getLockMode());
        q.setParameter("tenantRef", resolver.getSharedTenantRef());
        q.executeUpdate();
        executor.clearCache(ProcessDefinitionDTO.class.getSimpleName(), null);
        return ok();
    }
}
