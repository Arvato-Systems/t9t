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
package com.arvatosystems.t9t.embedded.tests.events

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.event.GenericEvent
import com.arvatosystems.t9t.base.request.ProcessEventRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class EventTest {

    static private ITestConnection dlg

    @BeforeAll
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def public void performEventLogTest() {
        val myEvent   = new GenericEvent => [
            eventID   = "testEvent"
            z         = #{ "hello" -> "world", "pi" -> 3.14 }
        ]
        dlg.okIO(new ProcessEventRequest => [
            eventHandlerQualifier = "logger"
            eventData             = myEvent
        ])
    }
}
