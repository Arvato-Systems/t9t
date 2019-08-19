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
package com.arvatosystems.t9t.remote.tests.simple

import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.request.BatchRequest
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.base.request.PauseRequest
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.CannedRequestRef
import com.arvatosystems.t9t.remote.connect.Connection
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO
import com.arvatosystems.t9t.ssm.SchedulerSetupKey
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceType
import com.arvatosystems.t9t.ssm.request.ClearAllRequest
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest
import de.jpaw.bonaparte.pojos.api.OperationType
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

class ITSchedulerLongTask {
    val REQUEST_ID      = "myPause"
    val SCHEDULER_ID    = "fastPause"

    @Test
    def void fastTriggerSchedulerTest() {
        val dlg = new Connection

        dlg.doIO(new ClearAllRequest)

        // batched task is a job to perform a log request, a pause, and another log, all in the same transaction
        val batchedTask = new BatchRequest => [
            commands = #[
                new LogMessageRequest("Hello BEFORE pause"),
                new PauseRequest => [
                    delayInMillis   = 4500
                ],
                new LogMessageRequest("Hello AFTER pause")
            ]
        ]

        val cannedRequestDTO = new CannedRequestDTO => [
            requestId                   = REQUEST_ID
            name                        = "pause request for test"
            // jobRequestObjectName        = batchedTask.ret$PQON
            // jobParameters               = #{ "delayInMillis" -> 4500 }
            request                     = batchedTask
        ]
//        val requestRef  = (dlg.typeIO(new CannedRequestCrudRequest => [
//            crud        = OperationType.MERGE
//            data        = cannedRequestDTO
//            naturalKey  = new CannedRequestKey(REQUEST_ID)
//        ], CrudSurrogateKeyResponse)).key
        val requestRef = cannedRequestDTO.merge(dlg).key

        val cfg = new SchedulerSetupDTO => [
            schedulerId                 = SCHEDULER_ID
            isActive                    = true
            userId                      = "admin"
            languageCode                = "en"
            name                        = "Pause request: 4 times pause of 4.5 seconds"
            recurrencyType              = SchedulerSetupRecurrenceType.FAST
            request                     = new CannedRequestRef(requestRef)
            repeatCount                 = 4
            intervalMilliseconds        = 500L
        ]

        // delete any old record to ensure we set up a new one. Ignore the result
        val key = new SchedulerSetupKey(SCHEDULER_ID)
        dlg.doIO(new SchedulerSetupCrudRequest => [
            crud        = OperationType.DELETE
            naturalKey  = key
        ])

        // create a new scheduler
        dlg.typeIO(new SchedulerSetupCrudRequest => [
            crud = OperationType.CREATE
            data = cfg
        ], CrudSurrogateKeyResponse)
    }

    @Test
    def void fastTriggerScheduler2Test() {
        val dlg = new Connection

        dlg.doIO(new ClearAllRequest)

        val cannedRequestDTO = new CannedRequestDTO => [
            requestId                   = REQUEST_ID
            name                        = "pause request for test"
            jobRequestObjectName        = (new PauseRequest).ret$PQON
            jobParameters               = #{ "delayInMillis" -> 4500 }
        ]
        val requestRef = cannedRequestDTO.merge(dlg).key

        val cfg = new SchedulerSetupDTO => [
            schedulerId                 = SCHEDULER_ID
            isActive                    = true
            userId                      = "admin"
            languageCode                = "en"
            name                        = "Pause request: 4 times pause of 4.5 seconds"
            recurrencyType              = SchedulerSetupRecurrenceType.FAST
            request                     = new CannedRequestRef(requestRef)
            repeatCount                 = 4
            intervalMilliseconds        = 500L
//            merge(dlg)
        ]

        // delete any old record to ensure we set up a new one. Ignore the result
        val key = new SchedulerSetupKey(SCHEDULER_ID)
        dlg.doIO(new SchedulerSetupCrudRequest => [
            crud        = OperationType.DELETE
            naturalKey  = key
        ])

        // create a new scheduler
        dlg.typeIO(new SchedulerSetupCrudRequest => [
            crud = OperationType.CREATE
            data = cfg
        ], CrudSurrogateKeyResponse)
    }
}
