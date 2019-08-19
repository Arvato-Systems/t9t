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
package com.arvatosystems.t9t.embedded.tests.simple

import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.CannedRequestKey
import com.arvatosystems.t9t.core.CannedRequestRef
import com.arvatosystems.t9t.core.request.CannedRequestCrudRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO
import com.arvatosystems.t9t.ssm.SchedulerSetupKey
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceType
import com.arvatosystems.t9t.ssm.request.ClearAllRequest
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest
import de.jpaw.bonaparte.pojos.api.OperationType
import org.junit.Test

class ITScheduler {
    val REQUEST_ID      = "myPing6"
    val SCHEDULER_ID    = "fastPing6"

    @Test
    def public void fastTriggerSchedulerTest() {
        val dlg = new InMemoryConnection

        dlg.doIO(new ClearAllRequest)

        val cannedRequestDTO = new CannedRequestDTO => [
            requestId                   = REQUEST_ID
            name                        = "ping request for test"
            request                     = new LogMessageRequest("Hello from the other side")
        ]
        val requestRef  = (dlg.typeIO(new CannedRequestCrudRequest => [
            crud        = OperationType.MERGE
            data        = cannedRequestDTO
            naturalKey  = new CannedRequestKey(REQUEST_ID)
        ], CrudSurrogateKeyResponse)).key

        val cfg = new SchedulerSetupDTO => [
            schedulerId                 = SCHEDULER_ID
            isActive                    = true
            userId                      = "admin"
            languageCode                = "en"
            name                        = "Another ping request for testing: 5 times"
            recurrencyType              = SchedulerSetupRecurrenceType.FAST
            request                     = new CannedRequestRef(requestRef)
            repeatCount                 = 4
            intervalMilliseconds        = 5000L
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

        Thread.sleep(30000L)        // allow the jobs to be executed
    }
}
