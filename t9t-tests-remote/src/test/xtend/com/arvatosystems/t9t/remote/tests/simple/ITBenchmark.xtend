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

import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.httppool.be.HttpClientPool
import com.arvatosystems.t9t.remote.connect.Connection
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.util.impl.RecordMarshallerBonaparte
import java.util.function.IntConsumer
import org.junit.Test

// benchmark the pooled connection vs the basic connection
@AddLogger
class ITBenchmark {
    val pingRequest = new PingRequest

    def void measure(int numberOfRuns, String what, IntConsumer runner) {
        var int totalExecs = 0;
        for (var int i = 0; i < numberOfRuns; i += 1) {
            val end = System.currentTimeMillis + 1000L
            var int j = 0
            do {
                j += 1
                runner.accept(j)
            } while (System.currentTimeMillis < end)
            LOGGER.info("Performed {} executions in one second for {}", j, what)
            totalExecs += j
        }
        LOGGER.info("Average is {} executions in one second for {}", totalExecs / numberOfRuns, what)
    }


    @Test
    def void testSimpleConnection() {
        val dlg = new Connection
        // warm up
        dlg.okIO(pingRequest)

        measure(5, "SIMPLE", [ dlg.okIO(pingRequest) ])
    }

    @Test
    def void testPooledConnection() {
        val dlg = new Connection
        val jwt = dlg.lastJwt
        val pool = new HttpClientPool("degtlun2952", 8325, 4, new RecordMarshallerBonaparte())
        // warm up
        pool.executeRequest(pingRequest, "/rpc", jwt)

        measure(5, "POOLED", [ pool.executeRequest(pingRequest, "/rpc", jwt) ])
    }
}
