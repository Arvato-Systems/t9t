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
package com.arvatosystems.t9t.embedded.tests.bpm

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowStep
import com.arvatosystems.t9t.bpmn.T9tWorkflow
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepAddParameters
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefResponse
import com.arvatosystems.t9t.bpmn.request.TriggerSingleProcessNowRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import java.util.ArrayList
import org.junit.BeforeClass
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.BpmExtensions.*

class BasicBpmTest {

    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def public void helloViaMessageSpecifiedInBpmTest() {
        // create a new task
        val stepList = new ArrayList<T9tAbstractWorkflowStep>()
        stepList.add(new T9tWorkflowStepJavaTask => [
            label      = "basicLog1"
            stepName   = "logger"
        ])
        stepList.add(new T9tWorkflowStepAddParameters => [
            label      = "enrichParameters"
            parameters = #{ "message" -> "hello world, what else?" }
        ])
        stepList.add(new T9tWorkflowStepJavaTask => [
            label      = "basicLog2"
            stepName   = "logger"
        ])
        new ProcessDefinitionDTO => [
            isActive   = true
            processDefinitionId    = "basicLogTest"
            name                   = "just some basic log test"
            initialParams          = #{ "message" -> "default message" }
            workflow               = new T9tWorkflow => [
                steps              = stepList
            ]
            merge(dlg)
        ]

        // run it!
        val ref = dlg.typeIO(new ExecuteProcessWithRefRequest => [
            targetObjectRef         = 28L
            processDefinitionId     = "basicLogTest"
            initialDelay            = 0  // no delay, but non null prevents the async triggering
        ], ExecuteProcessWithRefResponse).processCtrlRef

        dlg.okIO(new TriggerSingleProcessNowRequest => [
            processStatusRef        = ref
        ])

        // run it with an overridden initial message
        val ref2 = dlg.typeIO(new ExecuteProcessWithRefRequest => [
            targetObjectRef         = 464654L
            processDefinitionId     = "basicLogTest"
            initialDelay            = 0  // no delay, but non null prevents the async triggering
            initialParameters       = #{ "message" -> "a different initial message" }
        ], ExecuteProcessWithRefResponse).processCtrlRef

        dlg.okIO(new TriggerSingleProcessNowRequest => [
            processStatusRef        = ref2
        ])

    }
}
