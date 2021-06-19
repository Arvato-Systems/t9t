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
package com.arvatosystems.t9t.embedded.tests.bpm

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowStep
import com.arvatosystems.t9t.bpmn.T9tWorkflow
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepAddParameters
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefResponse
import com.arvatosystems.t9t.bpmn.request.ProcessExecutionStatusSearchRequest
import com.arvatosystems.t9t.bpmn.request.TriggerSingleProcessNowRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.annotations.AddLogger
import java.util.ArrayList
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static extension com.arvatosystems.t9t.misc.extensions.BpmExtensions.*
import de.jpaw.bonaparte.util.ToStringHelper
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW
import de.jpaw.bonaparte.pojos.api.NoTracking

@AddLogger
class BpmRestartParamsTest {

    static ITestConnection dlg

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    def checkNumExecs(int expected) {
        val resp = dlg.typeIO((new ProcessExecutionStatusSearchRequest), ReadAllResponse)
        LOGGER.info("currently we have {} exec status entries ({} expected)", resp.dataList.size, expected)
        if (!resp.dataList.empty) {
            val d0 = resp.dataList.get(0) as DataWithTrackingW<ProcessExecutionStatusDTO, NoTracking>
            val dto = d0.data
            LOGGER.info("    params are: {}", ToStringHelper.toStringSL(dto.currentParameters))
        }
    }

    @Test
    def void BpmDelayOnceTest() {
        val myTargetObjectRef = 4201L

        // create a new task
        val stepList = new ArrayList<T9tAbstractWorkflowStep>()
        stepList.add(new T9tWorkflowStepJavaTask => [
            label      = "basicLog1"
            stepName   = "logAll"
        ])
        stepList.add(new T9tWorkflowStepAddParameters => [
            label      = "enrichParameters"
            parameters = #{ "message" -> "hello world, what else?" }
        ])
        stepList.add(new T9tWorkflowStepJavaTask => [
            label      = "do a once time delay"
            stepName   = "delayOnce"
        ])
        stepList.add(new T9tWorkflowStepJavaTask => [
            label      = "basicLog2"
            stepName   = "logAll"
        ])
        new ProcessDefinitionDTO => [
            isActive   = true
            alwaysRestartAtStep1   = true
            processDefinitionId    = "bpmDelayOnceTest"
            name                   = "run twice, delay once"
            initialParams          = #{ "message" -> "default message", "delayOnceSeconds" -> 5 }
            workflow               = new T9tWorkflow => [
                steps              = stepList
            ]
            merge(dlg)
        ]

        checkNumExecs(0)

        // run it!
        val ref = dlg.typeIO(new ExecuteProcessWithRefRequest => [
            targetObjectRef         = myTargetObjectRef
            processDefinitionId     = "bpmDelayOnceTest"
            initialDelay            = 0  // no delay, but non null prevents the async triggering
        ], ExecuteProcessWithRefResponse).processCtrlRef
        checkNumExecs(1)

        dlg.okIO(new TriggerSingleProcessNowRequest => [
            processStatusRef        = ref
        ])
        checkNumExecs(1)

        // run it
        val ref2 = dlg.typeIO(new ExecuteProcessWithRefRequest => [
            targetObjectRef         = myTargetObjectRef
            processDefinitionId     = "bpmDelayOnceTest"
            initialDelay            = 0  // no delay, but non null prevents the async triggering
        ], ExecuteProcessWithRefResponse).processCtrlRef

        checkNumExecs(1)
        dlg.okIO(new TriggerSingleProcessNowRequest => [
            processStatusRef        = ref2
        ])

        checkNumExecs(1)
        Thread.sleep(10)
        checkNumExecs(1)

        dlg.okIO(new TriggerSingleProcessNowRequest => [
            processStatusRef        = ref2
        ])
        checkNumExecs(0)
        Thread.sleep(10)
    }
}
