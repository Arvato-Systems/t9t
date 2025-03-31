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
package com.arvatosystems.t9t.embedded.tests.bpm

import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.bpmn.IWorkflowStep
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO
import com.arvatosystems.t9t.bpmn.T9tWorkflow
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepAddParameters
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefResponse
import com.arvatosystems.t9t.bpmn.request.TriggerSingleProcessNowRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import java.util.UUID
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static extension com.arvatosystems.t9t.misc.extensions.BpmExtensions.*

class BpmnErrorTest extends AbstractBpmnTest {

    static ITestConnection dlg

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void regularErrorReturnInBpmTest() {
        val setup = new SetupUserTenantRole(dlg)
        setup.createUserTenantRole("bpmnErr1", UUID.randomUUID, true)

        new ProcessDefinitionDTO => [
            isActive   = true
            processDefinitionId    = "basicErrorTest"
            name                   = "test that workflow stops in case of exception"
            workflow               = new T9tWorkflow => [
                steps              = #[
                    new T9tWorkflowStepAddParameters => [
                          label      = "set custom error code"
                        parameters = #{ IWorkflowStep.PROCESS_VARIABLE_RETURN_CODE -> 4711 }
                    ],
                    new T9tWorkflowStepJavaTask => [
                        label      = "errorStep"
                        stepName   = "error"
                    ]
                ]
            ]
            merge(dlg)
        ]

        // run it!
        val ref = dlg.typeIO(new ExecuteProcessWithRefRequest => [
            targetObjectRef         = 28L
            processDefinitionId     = "basicErrorTest"
            initialDelay            = 0  // no delay, but non null prevents the async triggering
        ], ExecuteProcessWithRefResponse).processCtrlRef

        dlg.okIO(new TriggerSingleProcessNowRequest => [
            processStatusRef        = ref
        ])

        // check for process status in error
        checkNumExecs(dlg, 1, 4711)
    }
}