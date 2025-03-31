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
package com.arvatosystems.t9t.embedded.tests.simple

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.CannedRequestKey
import com.arvatosystems.t9t.core.request.ExecuteCannedRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

class CannedRequestTest {

    static ITestConnection dlg

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void helloByDtoWithParametersTest() {
        val dto = new CannedRequestDTO => [
            requestId               = "helloTest1"
            name                    = "helloTest1"
            jobParameters           = #{ "message" -> "hello, first world" }
            jobRequestObjectName    = "t9t.base.request.LogMessageRequest"
            validate
        ]

        dlg.okIO(new ExecuteCannedRequest(dto))
    }

    @Test
    def void helloByDtoWithObjectTest() {
        val dto = new CannedRequestDTO => [
            requestId               = "helloTest2"
            name                    = "helloTest2"
            request                 = new LogMessageRequest("hello, second world")
        ]

        dlg.okIO(new ExecuteCannedRequest(dto))
    }

    @Test
    def void helloWithPersistenceTest() {
        // create a canned request and persist it
        new CannedRequestDTO => [
            requestId               = "helloTestP"
            name                    = "helloTestP"
            jobParameters           = #{ "message" -> "hello, persisted world" }
            jobRequestObjectName    = "t9t.base.request.LogMessageRequest"
            merge(dlg)
        ]
        dlg.okIO(new ExecuteCannedRequest(new CannedRequestKey("helloTestP")))
    }

    @Test
    def void objectWithInstantTest() {
        // create a canned request and persist it
        new CannedRequestDTO => [
            requestId               = "rerun"
            name                    = "Request to rerun some transactions"
            jobParameters           = #{
                "fromDate" -> 1732474800,
                "toDate"   -> 1732484800,
                "stopOnError" -> false,
                "pqon" -> "t9t.base.request.PingRequest"
            }
            jobRequestObjectName    = "t9t.msglog.request.RerunFailedRequestsRequest"
            merge(dlg)
        ]
        dlg.okIO(new ExecuteCannedRequest(new CannedRequestKey("rerun")))
    }

//    @Test
//    def void objectWithInstantStringTest() {
//        // create a canned request and persist it
//        new CannedRequestDTO => [
//            requestId               = "rerunAlnum"
//            name                    = "Request to rerun some transactions"
//            jobParameters           = #{
//                "fromDate" -> "2024-11-24T12:34:56Z",
//                "toDate"   -> "2024-11-24T23:45:01Z",
//                "stopOnError" -> false,
//                "pqon" -> "t9t.base.request.PingRequest"
//            }
//            jobRequestObjectName    = "t9t.msglog.request.RerunFailedRequestsRequest"
//            merge(dlg)
//        ]
//        dlg.okIO(new ExecuteCannedRequest(new CannedRequestKey("rerunAlnum")))
//    }
}
