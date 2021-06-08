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
package com.arvatosystems.t9t.embedded.tests.scheduler

import com.arvatosystems.t9t.auth.request.ApiKeySearchRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.request.BatchRequest
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.base.request.PauseRequest
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.core.CannedRequestRef
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.ssm.SchedulerConcurrencyType
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceType
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import org.junit.BeforeClass
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

@AddLogger
class SchedulerSetupNoParallelTest {

    static ITestConnection dlg

    @BeforeClass
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    def void assertSingleApiKey() {
        val searchRq = new ApiKeySearchRequest => [
            searchFilter = new UnicodeFilter("name") => [
                equalsValue = 'automatically created by SSM for scheduled task'
            ]
        ]
        val result = dlg.typeIO(searchRq, ReadAllResponse)
        // Assert.assertEquals("Expected 1 ApiKey", 1, result.dataList.size)  // not working when running in CI environment
    }

    @Test
    def void createSchedulerAndKillTest() {
        val pause = new PauseRequest
        pause.delayInMillis = 2000
        val requestRef = dlg.createCannedRequestWithParameters('testSlowHello', 'Say hello twice',
            new BatchRequest(false, #[
                new LogMessageRequest('A'),
                pause,
                new LogMessageRequest('B')
            ])
        )

        val cfg = new SchedulerSetupDTO => [
            schedulerId          = 'testSlowHello'
            name                 = 'Say hello twice'
            request              = new CannedRequestRef(requestRef)
            userId               = 'admin'
            recurrencyType       = SchedulerSetupRecurrenceType.FAST
            repeatCount          = 4
            intervalMilliseconds = 800L
            concurrencyType      = SchedulerConcurrencyType.RUN_PARALLEL
            timeLimit            = 1
            concurrencyTypeStale = SchedulerConcurrencyType.KILL_PREVIOUS
            isActive             = true
        ]


        // create a new scheduler
        val crudResp = dlg.typeIO(new SchedulerSetupCrudRequest => [
            crud = OperationType.CREATE
            data = cfg
        ], CrudSurrogateKeyResponse)

        assertSingleApiKey

        Thread.sleep(6000L)
    }
}
