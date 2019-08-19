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

import java.util.List;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowStep;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessDefinitionEntity;
import com.arvatosystems.t9t.bpmn.jpa.mapping.IProcessDefinitionDTOMapper;
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessDefinitionEntityResolver;
import com.arvatosystems.t9t.bpmn.request.ProcessDefinitionCrudRequest;

import de.jpaw.dp.Jdp;

public class ProcessDefinitionCrudRequestHandler extends AbstractCrudSurrogateKey42RequestHandler<ProcessDefinitionRef, ProcessDefinitionDTO, FullTrackingWithVersion, ProcessDefinitionCrudRequest, ProcessDefinitionEntity> {
    protected final IProcessDefinitionEntityResolver resolver = Jdp.getRequired(IProcessDefinitionEntityResolver.class);
    protected final IProcessDefinitionDTOMapper mapper = Jdp.getRequired(IProcessDefinitionDTOMapper.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public CrudSurrogateKeyResponse<ProcessDefinitionDTO, FullTrackingWithVersion> execute(final ProcessDefinitionCrudRequest request) throws Exception {
        executor.clearCache(ProcessDefinitionDTO.class.getSimpleName(), null);
        return execute(mapper, resolver, request);
    }

    protected void validateSteps(List<T9tAbstractWorkflowStep> steps) {
        for (T9tAbstractWorkflowStep step: steps) {
            if (step.getLabel() == null) {
                throw new T9tException(T9tBPMException.BPM_NO_LABEL, step.getComment());
            }
        }
    }

    @Override
    protected void validateUpdate(ProcessDefinitionEntity current, ProcessDefinitionDTO intended) {
        validateSteps(intended.getWorkflow().getSteps());
    }
    @Override
    protected void validateCreate(ProcessDefinitionDTO intended) {
        validateSteps(intended.getWorkflow().getSteps());
    }
}
