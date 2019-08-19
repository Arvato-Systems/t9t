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
package com.arvatosystems.t9t.misc.extensions

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO
import com.arvatosystems.t9t.bpmn.ProcessDefinitionKey
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowStep
import com.arvatosystems.t9t.bpmn.T9tWorkflow
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask
import com.arvatosystems.t9t.bpmn.request.ProcessDefinitionCrudRequest
import com.arvatosystems.t9t.io.DataSinkDTO
import de.jpaw.bonaparte.pojos.api.OperationType
import java.util.ArrayList
import java.util.List

class BpmExtensions {

    // extension methods for the types with surrogate keys
    def static CrudSurrogateKeyResponse<DataSinkDTO, FullTrackingWithVersion> merge(ProcessDefinitionDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ProcessDefinitionCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new ProcessDefinitionKey(dto.processDefinitionId)
        ], CrudSurrogateKeyResponse)
    }

    def static T9tWorkflow toBasicWorkflowOfJavaSteps(List<String> stepNames) {
        val stepList = new ArrayList<T9tAbstractWorkflowStep>(stepNames.size)
        var int stepNo = 0
        for (s : stepNames) {
            stepNo += 10
            val step      = new T9tWorkflowStepJavaTask
            step.label    = String.format("L%03d", stepNo)
            step.stepName = s
            stepList.add(step)
        }
        return new T9tWorkflow => [
            steps = stepList
        ]
    }
}
