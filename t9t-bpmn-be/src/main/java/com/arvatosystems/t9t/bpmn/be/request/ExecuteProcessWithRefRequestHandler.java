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

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefResponse;
import com.arvatosystems.t9t.bpmn.services.IBpmnPersistenceAccess;
import com.arvatosystems.t9t.bpmn.services.IProcessDefinitionCache;

import de.jpaw.dp.Jdp;

public class ExecuteProcessWithRefRequestHandler extends AbstractRequestHandler<ExecuteProcessWithRefRequest> {
    protected final IBpmnPersistenceAccess persistenceAccess = Jdp.getRequired(IBpmnPersistenceAccess.class);
    protected final IProcessDefinitionCache pdCache = Jdp.getRequired(IProcessDefinitionCache.class);

    @Override
    public ExecuteProcessWithRefResponse execute(RequestContext ctx, ExecuteProcessWithRefRequest rq) {
        // this just creates the entry in the process execution table, and then launches the process asynchronously
        // 1.) validate that the processId exists
        ProcessDefinitionDTO pd = pdCache.getCachedProcessDefinitionDTO(ctx.tenantId, rq.getProcessDefinitionId());

        // 2.) create or validate the entry in the status table
        ProcessExecutionStatusDTO newStatus = new ProcessExecutionStatusDTO();
        newStatus.setProcessDefinitionId(rq.getProcessDefinitionId());
        newStatus.setTargetObjectRef(rq.getTargetObjectRef());
        newStatus.setCurrentParameters(rq.getInitialParameters() != null ? rq.getInitialParameters() : pd.getInitialParams());
        newStatus.setYieldUntil(rq.getInitialDelay() != null ? ctx.executionStart.plus(rq.getInitialDelay() * 1000L) : ctx.executionStart);

        // handle restartAtBeginning; potential request parameter overrules process definition config
        if (rq.getRestartAtBeginningIfExists()==null) {
            rq.setRestartAtBeginningIfExists(pd.getAlwaysRestartAtStep1());
        }
        Long ref = persistenceAccess.createOrUpdateNewStatus(ctx, newStatus, rq);

        ExecuteProcessWithRefResponse resp = new ExecuteProcessWithRefResponse();
        resp.setProcessCtrlRef(ref == null ? Long.valueOf(0L) : ref);  // special case: did not exist and "IGNORE IF NEW"
        return resp;
    }
}
