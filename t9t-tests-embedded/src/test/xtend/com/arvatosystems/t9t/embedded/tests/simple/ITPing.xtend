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
import com.arvatosystems.t9t.base.request.ExceptionRequest
import com.arvatosystems.t9t.base.request.ExecuteAsyncRequest
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.base.request.PauseRequest
import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.base.request.ProcessStatusRequest
import com.arvatosystems.t9t.base.request.ProcessStatusResponse
import com.arvatosystems.t9t.embedded.connect.Connection
import org.junit.Test
import com.arvatosystems.t9t.base.request.ContextlessLogMessageRequest

class ITPing {
    @Test
    def public void pingTest() {            // 1 ping
        val dlg = new Connection

        dlg.okIO(new PingRequest)
    }

    @Test
    def public void ping20Test() {          // 20 pings
        val dlg = new Connection

        for (var int i = 0; i < 20; i += 1)
            dlg.okIO(new PingRequest)
    }

    def private queryAndPrintProcessStatus(ITestConnection dlg) {
        val resp = dlg.typeIO(new ProcessStatusRequest, ProcessStatusResponse)
        println('''
            Active processes are:
            «FOR e: resp.processes»
                «e»
            «ENDFOR»
        ''')
    }

    @Test
    def public void psTest() {            // 1 ping
        val dlg = new Connection
        dlg.queryAndPrintProcessStatus
    }


    @Test
    def public void psMultiTest() {            // 1 ping
        val dlg = new Connection

        // launch 10 requests which take a while...
        for (var int i = 0; i < 10; i += 1) {
            dlg.okIO(new ExecuteAsyncRequest(new PauseRequest(i, 1000)))     // 1 sec pause
            Thread.sleep(50L)   // wait 50 ms
        }
        // query status now
        dlg.queryAndPrintProcessStatus

        // wait a sec, then again
        Thread.sleep(1000L)
        dlg.queryAndPrintProcessStatus

        // wait a sec, then again
        Thread.sleep(1000L)
        dlg.queryAndPrintProcessStatus
    }

    @Test
    def public void exceptionAndPingTest() {            // 1 ping after an exception
        val dlg = new Connection

        dlg.doIO(new ExceptionRequest(334756835, "expected error"))
        dlg.okIO(new PingRequest)
    }

    @Test
    def public void asyncHelloTest() {
        val dlg = new Connection

        dlg.okIO(new ExecuteAsyncRequest(new LogMessageRequest("hello, world")))

        Thread.sleep(2000L)  // see the result of async...
    }

    @Test
    def public void helloTest() {
        val dlg = new Connection

        dlg.okIO(new LogMessageRequest("hello, world"))
    }

    @Test
    def public void helloAsyncAsyncAsyncTest() {
        val dlg = new Connection

        dlg.okIO(
            new ExecuteAsyncRequest(
                new ExecuteAsyncRequest(
                    new ExecuteAsyncRequest(
                        new ExecuteAsyncRequest(
                            new ExecuteAsyncRequest(
                                new ExecuteAsyncRequest(
                                    new LogMessageRequest("hello, world")
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    def public void contextlessMessageTest() {
        val dlg = new Connection

        dlg.okIO(new LogMessageRequest("hello, world"))
        dlg.okIO(new ContextlessLogMessageRequest("hello, world (no context)"))
        dlg.okIO(new ContextlessLogMessageRequest("null"))
        dlg.okIO(new ContextlessLogMessageRequest("hello, world (no context)"))
    }
}
