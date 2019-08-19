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

import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRoleForBenchmarks
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.entities.WriteTracking
import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.base.request.SimpleBenchmarkRequest
import com.arvatosystems.t9t.base.request.SimpleBenchmarkResponse
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.batch.StatisticsDTO
import com.arvatosystems.t9t.batch.request.LogStatisticsRequest
import com.arvatosystems.t9t.batch.request.StatisticsCrudRequest
import com.arvatosystems.t9t.batch.request.StatisticsSearchRequest
import com.arvatosystems.t9t.event.SubscriberConfigDTO
import com.arvatosystems.t9t.event.SubscriberConfigKey
import com.arvatosystems.t9t.event.request.SubscriberConfigCrudRequest
import com.arvatosystems.t9t.remote.connect.Connection
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import java.util.UUID
import org.joda.time.Instant
import org.junit.Assert
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

class ITCacheBenchmark {
    val WARMUP_COUNT = 1000
    val REAL_COUNT = 10000
    val SUBSCRIBER_KEY = new SubscriberConfigKey => [
        eventID             = "originalEventID"
        handlerClassName    = "logger"
    ]

    def void benchmark(ITestConnection dlg, RequestParameters rq, boolean auto, String what) {
        val benchmarkRq = new SimpleBenchmarkRequest => [
            numberOfIterations = WARMUP_COUNT
            runAutonomous      = auto
            mustCopyRequest    = true
            request            = rq
        ]
        dlg.typeIO(benchmarkRq, SimpleBenchmarkResponse) // warmup

        benchmarkRq.numberOfIterations = REAL_COUNT
        val resp1 = dlg.typeIO(benchmarkRq, SimpleBenchmarkResponse)  // the real one
        println('''Benchmark results: MIN «resp1.minNanos» ns, MAX «resp1.maxNanos», AVG «resp1.avgNanos» at autonomous = «auto» for «what»''')
    }

    def void benchmark(ITestConnection dlg, RequestParameters rq, String what) {
        benchmark(dlg, rq, false, what)
        benchmark(dlg, rq, true, what)
    }

    def RequestParameters createUncachedReadByPk(ITestConnection dlg, String randomData) {
        // create test data
        val stat = new StatisticsDTO => [
            processId           = "Benchmark"
            startTime           = new Instant
            endTime             = startTime
            recordsProcessed    = 1
            recordsError        = 0
            info1               = "dummy entry"
            info2               = randomData
        ]
        dlg.okIO(new LogStatisticsRequest(stat))
        // obtain its ID
        val sresp = dlg.typeIO(new StatisticsSearchRequest => [
            searchFilter = new UnicodeFilter("info2") => [
                equalsValue = randomData
            ]
        ], ReadAllResponse) as ReadAllResponse<StatisticsDTO, WriteTracking>

        Assert.assertTrue(sresp.dataList.size == 1)
        val statisticsObjectRef = sresp.dataList.get(0).data.objectRef

        val readUncachedByPkRequest = new StatisticsCrudRequest => [
            crud    = OperationType.READ
            key     = statisticsObjectRef
        ]
        // validate it's working
        val res1 = dlg.typeIO(readUncachedByPkRequest, CrudSurrogateKeyResponse) as CrudSurrogateKeyResponse<StatisticsDTO, WriteTracking>
        Assert.assertEquals(randomData, res1.data.info2)
         return readUncachedByPkRequest
    }

    def RequestParameters createCachedReadByPk(ITestConnection dlg) {
        new SubscriberConfigDTO => [
            isActive            = true
            eventID             = SUBSCRIBER_KEY.eventID
            handlerClassName    = SUBSCRIBER_KEY.handlerClassName
            merge(dlg)
        ]
        val readRq = new SubscriberConfigCrudRequest => [
            crud       = OperationType.READ
            naturalKey = SUBSCRIBER_KEY
        ]
        // validate it's working
        val res1 = dlg.typeIO(readRq, CrudSurrogateKeyResponse) as CrudSurrogateKeyResponse<SubscriberConfigDTO, FullTrackingWithVersion>
        Assert.assertEquals(SUBSCRIBER_KEY.eventID, res1.data.eventID)
        return new SubscriberConfigCrudRequest => [
            crud       = OperationType.READ
            key        = res1.data.objectRef
        ]
    }

    @Test
    def void fullBenchmarkTest() {
        val dlg = new Connection

        val setup = new SetupUserTenantRoleForBenchmarks(dlg)

        val newKey = UUID.randomUUID
        setup.createUserTenantRole("cacheTest", newKey, true)

        val readUncachedByPkRequest = createUncachedReadByPk(dlg, newKey.toString)
        val readCachedByPkRequest   = createCachedReadByPk(dlg)
        val readUncachedRequest     = new SubscriberConfigCrudRequest => [
            crud       = OperationType.READ
            naturalKey = SUBSCRIBER_KEY
        ]

        dlg.benchmark(new PingRequest, "PING")
        dlg.benchmark(readUncachedByPkRequest, "STATISTICS (uncached PK)")
        dlg.benchmark(readCachedByPkRequest,   "SUBSCRIBER (cached PK)")
        dlg.benchmark(readUncachedRequest,     "SUBSCRIBER (alternate)")
    }
}
