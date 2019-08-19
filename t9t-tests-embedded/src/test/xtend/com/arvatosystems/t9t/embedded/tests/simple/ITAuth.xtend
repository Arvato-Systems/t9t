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

import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.embedded.connect.Connection
import java.util.UUID
import org.junit.Test

class ITAuth {
    static final UUID DUMMY_API_KEY = UUID.fromString("896d22d1-a332-438e-b2f8-2a539c29b8ca")

    @Test
    def public void pingTest() {
        val dlg = new Connection
        dlg.switchUser(DUMMY_API_KEY)
        dlg.okIO(new PingRequest)
    }
    @Test
    def public void ping2Test() {
        val dlg = new Connection(DUMMY_API_KEY)

        dlg.okIO(new PingRequest)
    }
}
