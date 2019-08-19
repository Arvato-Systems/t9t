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
package com.arvatosystems.t9t.bpmn.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusRef
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessDefinitionEntity
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessExecStatusEntity
import java.util.List

@AutoResolver42
class BpmnResolvers {

    @AllCanAccessGlobalTenant
    def ProcessDefinitionEntity       getProcessDefinitionEntity(ProcessDefinitionRef entityRef, boolean onlyActive) { return null; }
    def List<ProcessDefinitionEntity> findByProcessIdWithDefault(boolean onlyActive, String processDefinitionId) { return null; }
    def ProcessExecStatusEntity       getProcessExecStatusEntity(ProcessExecutionStatusRef ref, boolean onlyActive) { return null; }
    def List<ProcessExecStatusEntity> findByProcessId(boolean onlyActive, String processDefinitionId) { return null; }
    def ProcessExecStatusEntity       findByProcessDefinitionIdAndTargetObjectRef(boolean onlyActive, String processDefinitionId, Long targetObjectRef) { return null; }
}
