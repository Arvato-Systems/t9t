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
import com.arvatosystems.t9t.io.request.FlushPendingAsyncRequest
import com.arvatosystems.t9t.io.request.PerformAsyncRequest
import com.arvatosystems.t9t.remote.connect.Connection
import org.junit.Test
import de.jpaw.bonaparte.util.ToStringHelper

class ITPerformAsync {
    @Test
    def void asyncTest() {
        val dlg = new Connection
        val xml = new PingRequest


        dlg.okIO(new PerformAsyncRequest => [
            asyncChannelId = "test"
            payload        = xml
        ])
    }

    @Test
    def void asyncResultTest() {
        val dlg = new Connection
        val resp = dlg.okIO(new FlushPendingAsyncRequest => [
            markAsDone = true
            exportToFile = true
        ])
        println('''Result is «ToStringHelper.toStringML(resp)»''')
    }
}
