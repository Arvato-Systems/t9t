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

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.BPMNExtensionHelper.getAllProperties;
import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.BPMNExtensionHelper.getProperty;

import java.util.Map;
import java.util.Optional;

import javax.persistence.OptimisticLockException;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn2.IBPMNInitWorkflowCallback;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.BPMNVariableScopeReadOnlyContext;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.MDCHelper;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.WorkflowStepParameterMapAdapter;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
public class WorkflowStartContextListener implements ExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowStartContextListener.class);

    public static final String PROPERTY_WORKFLOW_TYPE = "WorkflowType";
    public static final String PROPERTY_INIT_WORKFLOW = "InitWorkflow";

    private final RepositoryService repositoryService = Jdp.getRequired(RepositoryService.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        try (AutoCloseable mdc = MDCHelper.put(execution)) {
            final Map<String, String> extensionProperties = getAllProperties(execution.getBpmnModelElementInstance());

            if ("true".equalsIgnoreCase(extensionProperties.getOrDefault(PROPERTY_INIT_WORKFLOW, "false"))) {
                final String workflowTypeString = getWorkflowType(execution);

                if (workflowTypeString != null) {
                    LOGGER.debug("Get lock refs for workflow with type '{}'", workflowTypeString);
                    final IBPMNInitWorkflowCallback initCallback = Jdp.getRequired(IBPMNInitWorkflowCallback.class, workflowTypeString);
                    initCallback.lockRefs(new BPMNVariableScopeReadOnlyContext(execution));

                    LOGGER.debug("Init workflow with type '{}'", workflowTypeString);
                    initCallback.init(new WorkflowStepParameterMapAdapter(execution), extensionProperties);
                }
            }
        } catch (ApplicationException e) {

            if (e.getErrorCode() == T9tException.NOT_CURRENT_RECORD_OPTIMISTIC_LOCKING) {
                // Wrap into OptimisticLockException to get appropriate support by BPMN engine
                throw new OptimisticLockException(e);
            } else {
                throw e;
            }
        }
    }

    /**
     * Get property WorkflowType on process level from process definition.
     *
     * @param execution Current execution
     *
     * @return Workflow type or NULL
     */
    private String getWorkflowType(DelegateExecution execution) {
        final String processDefinitionKey = repositoryService.getProcessDefinition(execution.getProcessDefinitionId()).getKey();

        // First get BPMN Model
        return Optional.of(repositoryService.getBpmnModelInstance(execution.getProcessDefinitionId()))
                       // Get process element of model
                       .map(model -> model.getModelElementById(processDefinitionKey))
                       .filter(element -> element instanceof Process)
                       // Now search for property PROPERTY_WORKFLOW_TYPE
                       .map(process -> getProperty((Process) process, PROPERTY_WORKFLOW_TYPE, null))
                       .orElse(null);
    }

}
