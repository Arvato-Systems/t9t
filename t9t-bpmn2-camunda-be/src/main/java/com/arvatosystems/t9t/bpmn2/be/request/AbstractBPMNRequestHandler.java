/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.IdentifierConverter.t9tUserRefToBPMNUserId;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import org.camunda.bpm.engine.IdentityService;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

/**
 * Derive all BPMN requests from this request to transfer request context data to the current BPMN engine instance.
 *
 * @author TWEL006
 */
public abstract class AbstractBPMNRequestHandler<R extends RequestParameters> extends AbstractRequestHandler<R> {

    private final IdentityService identityService = Jdp.getRequired(IdentityService.class);

    @Override
    public final ServiceResponse execute(RequestContext requestContext, R request) throws Exception {
        final String userRef = t9tUserRefToBPMNUserId(requestContext.getUserRef());
        final String tenantId = requestContext.tenantId;

        identityService.setAuthentication(userRef, emptyList(), asList(tenantId));

        try {
            return executeInWorkflowContext(requestContext, request);
        } finally {
            identityService.clearAuthentication();
        }
    }

    protected abstract ServiceResponse executeInWorkflowContext(RequestContext requestContext, R request) throws Exception;
}
