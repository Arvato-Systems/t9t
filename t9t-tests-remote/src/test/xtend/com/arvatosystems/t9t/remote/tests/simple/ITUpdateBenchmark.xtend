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

import com.arvatosystems.t9t.auth.RoleDTO
import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.request.RoleCrudRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRoleForBenchmarks
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.request.BatchRequest
import com.arvatosystems.t9t.base.request.SimpleBenchmarkRequest
import com.arvatosystems.t9t.base.request.SimpleBenchmarkResponse
import com.arvatosystems.t9t.remote.connect.Connection
import de.jpaw.bonaparte.pojos.api.OperationType
import java.util.UUID
import org.junit.Test

class ITUpdateBenchmark {
    val WARMUP_COUNT = 1000
    val REAL_COUNT = 10000

    def void benchmark(ITestConnection dlg, RequestParameters rq) {
        val benchmarkRq = new SimpleBenchmarkRequest => [
            numberOfIterations = WARMUP_COUNT
            runAutonomous      = true
            mustCopyRequest    = true
            request            = rq
        ]
        dlg.typeIO(benchmarkRq, SimpleBenchmarkResponse) // warmup

        benchmarkRq.numberOfIterations = REAL_COUNT
        val resp1 = dlg.typeIO(benchmarkRq, SimpleBenchmarkResponse)  // the real one
        println('''Benchmark results: MIN «resp1.minNanos» ns, MAX «resp1.maxNanos», AVG «resp1.avgNanos»''')
    }

    def protected asMerge(RoleDTO role) {
        return new RoleCrudRequest => [
            crud       = OperationType.MERGE
            data       = role
            naturalKey = new RoleKey("testRole")
        ]
    }

    def protected createRequest() {
        val role1 = new RoleDTO => [
            roleId         = "testRole"
            isActive       = true
            name           = "name 1"
        ]
        val role2 = new RoleDTO => [
            roleId         = "testRole"
            isActive       = true
            name           = "name 2"
        ]
        return new BatchRequest => [
            commands = #[ role1.asMerge, role2.asMerge ]
        ]
    }

    @Test
    def void fullBenchmarkTest() {
        val dlg = new Connection

        val setup = new SetupUserTenantRoleForBenchmarks(dlg)

        val newKey = UUID.randomUUID
        setup.createUserTenantRole("rwTest", newKey, true)

        val rq = createRequest
        dlg.benchmark(rq)
    }
}
