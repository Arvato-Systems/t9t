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
package com.arvatosystems.t9t.bpmn2.be.camunda.delegate;

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.WorkflowStepExecutionHelper.executeWorkflowStep;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn2.T9tBPMNConstants;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.MDCHelper;

import de.jpaw.dp.Singleton;

/**
 * <p>
 * Java delegate to execute a {@link IWorkflowStep}. The workflow return code of the executed step can be provided using
 * a given variable name.
 * </p>
 *
 * <p>
 * The workflow return code {@link WorkflowReturnCode#ERROR} also results in an BPMN error with error code
 * {@link T9tBPMNConstants#ERROR_CODE_STEP_ERROR}.
 * </p>
 *
 * <p>
 * Please note: The workflow return codes {@link WorkflowReturnCode#YIELD}, {@link WorkflowReturnCode#YIELD_NEXT},
 * {@link WorkflowReturnCode#COMMIT_RESTART} does not trigger and interrupt or commit and restarts in BPMN handling.
 * Furthermore, the return code {@link WorkflowReturnCode#DONE} does not stop the BPMN execution. If desired, the
 * workflow return code must be mapped to a variable and such handling must be provided direktly in BPMN.
 * </p>
 *
 * @author TWEL006
 */
@Singleton
public class WorkflowStepDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowStepDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try (AutoCloseable mdc = MDCHelper.put(execution)) {
            final String nameString = requireNonNull(Objects.toString(execution.getVariable("workflowStepName"), null),
                                                     "Variable 'workflowStepName' must not be empty");

            final WorkflowReturnCode returnCode = executeWorkflowStep(nameString, execution);

            execution.setVariableLocal("resultValue", returnCode);

            LOGGER.debug("End execute workflow step '{}' - return code is {}", nameString, returnCode);

            if (returnCode == WorkflowReturnCode.ERROR) {
                throw new BpmnError(T9tBPMNConstants.ERROR_CODE_STEP_ERROR);
            }
        }
    }

}
