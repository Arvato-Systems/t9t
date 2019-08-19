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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.request.DeployNewProcessRequest;
import com.arvatosystems.t9t.bpmn.request.DeployNewProcessResponse;
import com.arvatosystems.t9t.bpmn.services.IBPMService;

import de.jpaw.dp.Jdp;

/**
 * Implementation {@linkplain IRequestHandler} which handles {@linkplain DeployNewProcessRequest}.
 * @author LIEE001
 */
public class DeployNewProcessRequestHandler extends AbstractRequestHandler<DeployNewProcessRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployNewProcessRequestHandler.class);
    private final IBPMService bpmService = Jdp.getOptional(IBPMService.class);

    @Override
    public DeployNewProcessResponse execute(RequestContext requestCtx,final DeployNewProcessRequest request) throws Exception {
        DeployNewProcessResponse response = new DeployNewProcessResponse();
        if (bpmService == null) {
            LOGGER.error("Fail to lookup implementation for IBpmService. Please check your deployment package");
            throw new T9tException(T9tBPMException.BPM_NO_BPMN_ENGINE);
        }

        try {
            ProcessDefinitionDTO processDefinitionDTO = bpmService.deployNewProcess(request.getDeploymentComment(),
                    request.getContent().getBytes());

            response.setProcessDefinition(processDefinitionDTO);
            response.setReturnCode(0);
            return response;
        } catch (T9tBPMException ex) {
            // T9tBPMException signifies a technical exception
            LOGGER.error("Failed to deploy new process (tenantId: {}, userId: {}).", requestCtx.tenantId, requestCtx.userId, ex.getMessage());
            throw ex;
        }
    }
}
