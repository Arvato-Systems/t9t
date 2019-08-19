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
package com.arvatosystems.t9t.bpmn.services;

import java.util.Map;

import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn.pojo.ProcessDefinition;
import com.arvatosystems.t9t.bpmn.pojo.ProcessOutput;

/**
 * Service responsible for deploying, executing and getting metadata about
 * business processes defined as BPMN.
 * <p>
 * This service relies heavily on the existence of process definition entry in
 * <i>p42_cfg_process_definition</i> table. Every functionalities (except for
 * global processes and new process deployment) performed through this service
 * will first check if the passed process definition reference is actually exist
 * and active. Non-existent (or not active) record for the process definition
 * reference will throw {@linkplain T9tBPMN2Exception}.
 *
 * @author LIEE001
 */
public interface IBPMService {

    public static final String ENGINE_ACTIVITI = "activiti";
    public static final String BPMN_WORKFLOW_DATA = "bpmObject";
    public static final String CONTENT_RESOURCE = "content.bpmn20.xml";

    /**
     * Deploy a new business process.
     *
     * @param comment deployment comment
     * @param processDefinitionContent the process definition content
     * @return process definition configuration entity
     * @throws T9tBPMException if there is any error occurred during deployment
     */
    ProcessDefinitionDTO deployNewProcess(final String comment, final byte[] processDefinitionContent);

    /**
     * Re-deploy a single business process content.
     *
     * @param processDefinitionRef process definition reference
     * @param processDefinitionContent the process definition content
     * @throws T9tBPMException when the process definition with the given reference can't be found OR if there is an error in deployment plausibility check
     */
    void redeployProcess(ProcessDefinitionRef processDefinitionRef, byte[] processDefinitionContent);

    /**
     * Check if a business process is already deployed.
     * @param processDefinitionRef process definition reference
     * @return true if it's already deployed, false otherwise
     */
    boolean isProcessDeployed(ProcessDefinitionRef processDefinitionRef);

    /**
     * Deploy global business process.
     * @param processDefinition process definition
     */
    void deployGlobalProcess(ProcessDefinition processDefinition) ;

    /**
     * Check if a global business process is already deployed.
     * @param processDefinitionId global process definition id
     * @return true if it's already deployed, false otherwise
     */
    boolean isGlobalProcessDeployed(String processDefinitionId);

    /**
     * Execute a process.
     * @param processDefinitionId process id that identifies the process
     * @return process output
     * @ when there is an error executing the process
     */
    ProcessOutput executeProcess(String processDefinitionId) ;

    /**
     * Execute a process.
     * @param processDefinitionId process id that identifies the process
     * @param params process parameters to be used inside the process execution
     * @return process output
     * @ when there is an error executing the process
     */
    ProcessOutput executeProcess(String processDefinitionId, Map<String, ? extends Object> params) ;

    /**
     * Get process content for specific process definition.
     * @param processDefinitionRef process definition reference
     * @return process definition content (in bytes)
     * @throws T9tBPMException when the process definition with the given reference can't be found
     */
    byte[] getProcessContent(ProcessDefinitionRef processDefinitionRef);

    /**
     * Get process diagram for specific process definition.
     * @param processDefinitionRef process definition reference
     * @return process diagram for the process definition
     * @throws T9tBPMException when the process definition with the given reference can't be found OR
     *                              there is an error generating the process diagram
     */
    byte[] getProcessDiagram(ProcessDefinitionRef processDefinitionRef);

    /**
     * Execute process definition by process definition id synchronously.
     * @param processDefinitionId target process definition id
     * @param params process parameters to be used inside the process execution
     * @return process execution output
     * @ error happened during process execution
     */
    WorkflowReturnCode executeProcessSync(String processDefinitionId, Map<String, Object> params);

    void loadTenantProcess(String tenantId, String processDefinitionId);

}
