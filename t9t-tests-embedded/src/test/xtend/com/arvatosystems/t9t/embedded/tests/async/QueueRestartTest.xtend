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
package com.arvatosystems.t9t.embedded.tests.async

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.io.AsyncChannelDTO
import com.arvatosystems.t9t.io.AsyncQueueDTO
import com.arvatosystems.t9t.io.AsyncQueueKey
import com.arvatosystems.t9t.io.request.AsyncQueueCrudRequest
import com.arvatosystems.t9t.io.request.GetQueueStatusRequest
import com.arvatosystems.t9t.io.request.GetQueueStatusResponse
import com.arvatosystems.t9t.io.request.PerformAsyncRequest
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.OperationType
import org.junit.BeforeClass
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

class QueueRestartTest {
    static ITestConnection dlg

    @BeforeClass
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    def void printStatus() {
        println(dlg.typeIO(new GetQueueStatusRequest, GetQueueStatusResponse).status.map[toString].join("\n"))
    }

    @Test
    def void createAndShutdownQueueTest() {
        new AsyncQueueDTO => [
            asyncQueueId    = "TESTQUEUE"
            isActive        = true
            description     = "testing startup and shutdown"
            senderQualifier = "ERROR"
            validate
            merge(dlg)
        ]
        new AsyncChannelDTO => [
            isActive                = true
            asyncQueueRef           = new AsyncQueueKey("TESTQUEUE")
            asyncChannelId          = "test"
            description             = "channel for testing"
            url                     = "tbd"
            authType                = null
            authParam               = null
            maxRetries              = null
            payloadFormat           = MediaTypes.MEDIA_XTYPE_XML
            z                       = null
            merge(dlg)
        ]
        printStatus
        dlg.okIO(new PerformAsyncRequest => [
            asyncChannelId = "test"
            payload        = new PingRequest
        ])
        Thread.sleep(500L)
        printStatus
        dlg.okIO(new AsyncQueueCrudRequest => [
            crud       = OperationType.INACTIVATE
            naturalKey = new AsyncQueueKey("TESTQUEUE")
        ])
        printStatus
        Thread.sleep(1000L)
        printStatus
    }
}
