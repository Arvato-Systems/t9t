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

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.base.request.SimpleBenchmarkRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import org.junit.BeforeClass
import org.junit.Test

class BenchmarkTest {

    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def public void benchmarkPingTest() {
        val benchmarkRq = new SimpleBenchmarkRequest => [
            numberOfIterations = 1000
            runAutonomous      = false
            request            = new PingRequest
        ]
        dlg.okIO(benchmarkRq)  // warmup
        dlg.okIO(benchmarkRq)  // the real one
    }
    @Test
    def public void benchmarkPingAutonTest() {
        val benchmarkRq = new SimpleBenchmarkRequest => [
            numberOfIterations = 1000
            runAutonomous      = true
            request            = new PingRequest
        ]
        dlg.okIO(benchmarkRq)  // warmup
        dlg.okIO(benchmarkRq)  // the real one
    }
}
