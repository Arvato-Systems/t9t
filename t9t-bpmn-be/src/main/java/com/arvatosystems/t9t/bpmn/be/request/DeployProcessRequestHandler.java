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
package com.arvatosystems.t9t.bpmn.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.request.DeployProcessRequest;
import com.arvatosystems.t9t.bpmn.request.DeployProcessResponse;
import com.arvatosystems.t9t.bpmn.services.IBpmTechnicalService;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

/**
 * Implementation {@linkplain IRequestHandler} which handles {@linkplain DeployProcessRequest}.
 * @author LIEE001
 */
public class DeployProcessRequestHandler extends AbstractRequestHandler<DeployProcessRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployProcessRequestHandler.class);
    private final IBpmTechnicalService bpmService = Jdp.getOptional(IBpmTechnicalService.class);

    @Override
    public DeployProcessResponse execute(final RequestContext requestCtx, final DeployProcessRequest request) throws Exception {
        final DeployProcessResponse response = new DeployProcessResponse();

        if (bpmService == null) {
            LOGGER.error("Fail to lookup implementation for IBpmApplicationService. Please check your deployment package");
            throw new T9tException(T9tBPMException.BPM_NO_BPMN_ENGINE);
        }

        try {
            bpmService.redeployProcess(request.getProcessDefinitionRef(), request.getContent().getBytes());

            response.setWasDeployed(true);
            response.setReturnCode(0);
            return response;
        } catch (final T9tBPMException ex) {
            // T9tBPMException signifies a technical exception
            LOGGER.error("Failed to deploy process (tenantId: {}, userId: {}, processDefinitionRef: {}). {}",
                requestCtx.tenantId, requestCtx.userId, request.getProcessDefinitionRef(), ExceptionUtil.causeChain(ex));
            throw ex;
        }
    }
}
