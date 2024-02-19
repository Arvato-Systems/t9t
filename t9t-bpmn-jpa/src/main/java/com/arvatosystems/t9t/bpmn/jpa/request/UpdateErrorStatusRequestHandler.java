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
package com.arvatosystems.t9t.bpmn.jpa.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessExecStatusEntity;
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessExecStatusEntityResolver;
import com.arvatosystems.t9t.bpmn.request.UpdateErrorStatusRequest;

import de.jpaw.dp.Jdp;

/**
 * Called by BpmnRunner to set the error status of a process/workflow in case of a general exception.
 * Because of JPA transaction rollback, this needs to be outsourced.
 */
public class UpdateErrorStatusRequestHandler extends AbstractRequestHandler<UpdateErrorStatusRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateErrorStatusRequestHandler.class);

    private final IProcessExecStatusEntityResolver processExecStatusEntityResolver = Jdp.getRequired(IProcessExecStatusEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final UpdateErrorStatusRequest request) throws Exception {
        LOGGER.debug("Received UpdateErrorStatusRequest for status with ref {}", request.getProcessExecStatusRef());
        final ProcessExecStatusEntity entityData = processExecStatusEntityResolver.getEntityData(request.getProcessExecStatusRef(), true);
        entityData.setReturnCode(request.getReturnCode());
        entityData.setErrorDetails(MessagingUtil.truncField(request.getErrorDetails(),
                ProcessExecutionStatusDTO.meta$$errorDetails.getLength()));

        return ok();
    }
}
