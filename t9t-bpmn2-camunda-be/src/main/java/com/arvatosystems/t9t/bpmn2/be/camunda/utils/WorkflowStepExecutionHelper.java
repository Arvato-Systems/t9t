/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import static org.apache.commons.lang3.StringUtils.join;

import javax.persistence.OptimisticLockException;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn.WorkflowRunnableCode;
import com.arvatosystems.t9t.bpmn2.IBPMNObjectFactory;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public abstract class WorkflowStepExecutionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowStepExecutionHelper.class);

    /**
     * Execute T9T {@link IWorkflowStep} of given workflow step name with given execution scope.
     *
     * @param workflowStepName
     *            Workflow step name
     * @param executionScope
     *            Execution scope
     *
     * @return Result of step execution
     */
    public static WorkflowReturnCode executeWorkflowStep(String workflowStepName, VariableScope executionScope) {
        try {
            LOGGER.debug("Start execute workflow step '{}'", workflowStepName);

            final IWorkflowStep<Object> workflowStep = Jdp.getOptional(IWorkflowStep.class, workflowStepName);

            if (workflowStep == null) {
                throw new T9tException(T9tBPMException.BPM_STEP_NOT_FOUND, workflowStep);
            }

            final IBPMNObjectFactory<Object> objectFactory;
            if (workflowStep.getFactoryName() != null) {
                objectFactory = Jdp.getOptional(IBPMNObjectFactory.class, workflowStep.getFactoryName());
            } else {
                objectFactory = Jdp.getOptional(IBPMNObjectFactory.class);
            }

            if (objectFactory == null) {
                throw new T9tException(T9tBPMException.BPM_OBJECT_FACTORY_NOT_FOUND, workflowStep.getFactoryName());
            }

            final WorkflowStepParameterMapAdapter context = new WorkflowStepParameterMapAdapter(executionScope);
            final Object data = objectFactory.create(context);

            // Check what to do
            final WorkflowRunnableCode runnableCode = workflowStep.mayRun(data, context);

            if (runnableCode == null) {
                throw new T9tException(T9tBPMException.BPM_EXECUTE_JAVA_TASK_RETURNED_NULL, "mayRun");
            }

            final WorkflowReturnCode returnCode;
            switch (runnableCode) {
                case ERROR: {
                    returnCode = WorkflowReturnCode.ERROR;
                    break;
                }

                case SKIP: {
                    returnCode = WorkflowReturnCode.PROCEED_NEXT;
                    break;
                }

                case YIELD: {
                    returnCode = WorkflowReturnCode.YIELD;
                    break;
                }

                case RUN: {
                    returnCode = workflowStep.execute(data, context);
                    break;
                }

                default: {
                    throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, join("Workflow return code ", runnableCode.toString(), " is not implemented"));
                }
            }

            if (returnCode == null) {
                throw new T9tException(T9tBPMException.BPM_EXECUTE_JAVA_TASK_RETURNED_NULL, "execute");
            }

            return returnCode;
        } catch (ApplicationException e) {

            if (e.getErrorCode() == T9tException.NOT_CURRENT_RECORD_OPTIMISTIC_LOCKING) {
                // Wrap into OptimisticLockException to get appropriate support by BPMN engine
                throw new OptimisticLockException(e);
            } else {
                throw e;
            }
        }
    }
}
