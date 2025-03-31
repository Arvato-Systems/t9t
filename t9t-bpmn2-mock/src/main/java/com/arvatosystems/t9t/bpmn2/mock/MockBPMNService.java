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
package com.arvatosystems.t9t.bpmn2.mock;

import java.util.Map;

import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn.pojo.ProcessDefinition;
import com.arvatosystems.t9t.bpmn.pojo.ProcessOutput;
import com.arvatosystems.t9t.bpmn.services.IBpmTechnicalService;

import de.jpaw.dp.Singleton;

@Singleton
public class MockBPMNService implements IBpmTechnicalService {

    @Override
    public ProcessDefinitionDTO deployNewProcess(final String comment, final byte[] processDefinitionContent) {
        return null;
    }

    @Override
    public void redeployProcess(final ProcessDefinitionRef processDefinitionRef, final byte[] processDefinitionContent) {
    }

    @Override
    public boolean isProcessDeployed(final ProcessDefinitionRef processDefinitionRef) {
        return false;
    }

    @Override
    public void deployGlobalProcess(final ProcessDefinition processDefinition) {
    }

    @Override
    public boolean isGlobalProcessDeployed(final String processDefinitionId) {
        return false;
    }

    @Override
    public ProcessOutput executeProcess(final String processDefinitionId) {
        return null;
    }

    @Override
    public ProcessOutput executeProcess(final String processDefinitionId, final Map<String, ? extends Object> params) {
        return null;
    }

    @Override
    public byte[] getProcessContent(final ProcessDefinitionRef processDefinitionRef) {
        return null;
    }

    @Override
    public byte[] getProcessDiagram(final ProcessDefinitionRef processDefinitionRef) {
        return null;
    }

    @Override
    public WorkflowReturnCode executeProcessSync(final String processDefinitionId, final Map<String, Object> params) {
        return null;
    }

    @Override
    public void loadTenantProcess(final String tenantId, final String processDefinitionId) {
    }

}
