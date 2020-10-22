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
package com.arvatosystems.t9t.bpmn2.be.camunda.listener;

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.BPMNExtensionHelper.getProperty;
import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.WorkflowStepExecutionHelper.executeWorkflowStep;
import static org.apache.commons.lang3.StringUtils.join;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.MDCHelper;

import de.jpaw.dp.Singleton;

/**
 * <p>
 * Execute workflow step implementation on execution event.
 * </p>
 *
 * <p>
 * The workflow step implementation must return a {@link WorkflowReturnCode#DONE}. Any other return code will result in
 * an exception since the process is expected to end with this execution.
 * </p>
 */
@Singleton
public class ExecuteFinalWorkflowStepListener implements ExecutionListener {

    public static final String PROPERTY_WORKFLOW_STEP_NAME = "WorkflowStepName";

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        try (AutoCloseable mdc = MDCHelper.put(execution)) {
            final String workflowStepName = getProperty(execution.getBpmnModelElementInstance(), PROPERTY_WORKFLOW_STEP_NAME, null);

            if (workflowStepName != null) {
                final WorkflowReturnCode returnCode = executeWorkflowStep(workflowStepName, execution);

                if (returnCode != WorkflowReturnCode.DONE) {
                    throw new RuntimeException(join("Workflow step implementation did not provide workflow return code DONE - provided: ",
                                                    returnCode.toString()));
                }
            }
        }
    }

}
