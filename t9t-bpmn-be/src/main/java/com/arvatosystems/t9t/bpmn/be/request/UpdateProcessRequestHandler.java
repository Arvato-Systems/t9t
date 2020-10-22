/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefResponse;
import com.arvatosystems.t9t.bpmn.request.UpdateProcessRequest;
import com.arvatosystems.t9t.bpmn.request.WorkflowActionEnum;
import com.arvatosystems.t9t.bpmn.services.IBpmnPersistenceAccess;
import de.jpaw.dp.Jdp;

public class UpdateProcessRequestHandler extends AbstractRequestHandler<UpdateProcessRequest> {
    protected final IBpmnPersistenceAccess persistenceAccess = Jdp.getRequired(IBpmnPersistenceAccess.class);
    protected final IExecutor messaging = Jdp.getRequired(IExecutor.class);

    @Override
    public ExecuteProcessWithRefResponse execute(RequestContext ctx, UpdateProcessRequest rq) {
        ProcessExecutionStatusDTO dto = persistenceAccess.getProcessExecutionStatusDTO(rq.getProcessDefinitionId(), rq.getTargetObjectRef());
        ExecuteProcessWithRefRequest bpmRefRequest = new ExecuteProcessWithRefRequest();

        if(rq.getCurrentParameters() != null) {
            dto.setCurrentParameters(rq.getCurrentParameters());
        }

        if(rq.getNextStep() != null) {
            dto.setNextStep(rq.getNextStep());
        }

        if(rq.getYieldUntil() != null) {
            dto.setYieldUntil(rq.getYieldUntil());
        }

        bpmRefRequest.setTargetObjectRef(rq.getTargetObjectRef());
        bpmRefRequest.setProcessDefinitionId(rq.getProcessDefinitionId());
        bpmRefRequest.setIfNoEntryExists(WorkflowActionEnum.ERROR);
        bpmRefRequest.setRestartAtBeginningIfExists(false);
        bpmRefRequest.setIfEntryExists(WorkflowActionEnum.RUN);
        bpmRefRequest.setInitialDelay(0);

        if(Boolean.TRUE.equals(rq.getDirectProcessing())) {
            bpmRefRequest.setInitialDelay(null); // direct process trigger
        }

        Long ref = persistenceAccess.createOrUpdateNewStatus(ctx, dto, bpmRefRequest);
        ExecuteProcessWithRefResponse response = new ExecuteProcessWithRefResponse();
        response.setProcessCtrlRef(ref);

        return response;
    }
}
