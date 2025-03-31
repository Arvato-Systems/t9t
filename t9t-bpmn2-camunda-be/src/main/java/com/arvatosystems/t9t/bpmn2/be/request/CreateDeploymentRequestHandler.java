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
package com.arvatosystems.t9t.bpmn2.be.request;

import java.io.ByteArrayInputStream;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.request.CreateDeploymentRequest;
import com.arvatosystems.t9t.bpmn2.request.CreateDeploymentResponse;
import com.arvatosystems.t9t.bpmn2.request.DeploymentResourceDTO;

import de.jpaw.dp.Jdp;

public class CreateDeploymentRequestHandler extends AbstractBPMNRequestHandler<CreateDeploymentRequest> {

    private final RepositoryService repositoryService = Jdp.getRequired(RepositoryService.class);

    @Override
    protected CreateDeploymentResponse executeInWorkflowContext(RequestContext requestContext, CreateDeploymentRequest request) throws Exception {

        DeploymentBuilder builder = repositoryService.createDeployment()
                                                     .tenantId(requestContext.tenantId);

        if (request.getDeploymentName() != null) {
            builder = builder.name(request.getDeploymentName());
        }

        for (DeploymentResourceDTO resource : request.getResources()) {
            builder = builder.addInputStream(resource.getName(), new ByteArrayInputStream(resource.getData()
                                                                                                  .getBytes()));
        }

        final Deployment deployment = builder.deploy();

        final CreateDeploymentResponse response = new CreateDeploymentResponse();
        response.setReturnCode(0);
        response.setDeploymentId(deployment.getId());
        return response;
    }
}
