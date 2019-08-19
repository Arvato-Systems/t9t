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

import com.arvatosystems.t9t.base.event.GenericEvent
import com.arvatosystems.t9t.base.request.ProcessEventRequest
import com.arvatosystems.t9t.base.request.PublishEventRequest
import com.arvatosystems.t9t.event.SubscriberConfigDTO
import com.arvatosystems.t9t.remote.connect.Connection
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

class ITEvents {
    @Test
    def void processLoggerEventTest() {
        val dlg = new Connection

        dlg.okIO(new ProcessEventRequest => [
            eventHandlerQualifier  = "logger"
            eventData              = new GenericEvent => [
                eventID            = "originalEventID"
                z                  = #{ "near" -> 1.2, "far" -> "galaxy", "condition" -> true }
            ]
        ])
    }

    @Test
    def void configureSubscriptionTest() {
        val dlg = new Connection

        new SubscriberConfigDTO => [
            isActive            = true
            eventID             = "originalEventID"
            handlerClassName    = "logger"
            merge(dlg)
        ]
    }

    @Test
    def void fireSomeCaughtEventTest() {
        val dlg = new Connection

        dlg.okIO(new PublishEventRequest => [
            eventData              = new GenericEvent => [
                eventID            = "originalEventID"
                z                  = #{ "near" -> 1.2, "far" -> "galaxy", "condition" -> true }
            ]
        ])
    }

    @Test
    def void fireSomeUncaughtEventTest() {
        val dlg = new Connection

        dlg.okIO(new PublishEventRequest => [
            eventData              = new GenericEvent => [
                eventID            = "unknownEventID"
                z                  = #{ "near" -> 1.2, "far" -> "galaxy", "condition" -> true }
            ]
        ])
    }
}
