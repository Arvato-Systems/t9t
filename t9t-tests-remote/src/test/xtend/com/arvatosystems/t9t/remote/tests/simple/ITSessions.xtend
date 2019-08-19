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

import com.arvatosystems.t9t.auth.request.SessionSearchRequest
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.remote.connect.Connection
import de.jpaw.bonaparte.pojos.api.SortColumn
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW
import org.junit.Test

class ITSessions {
    @Test
    def void querySessionsTest() {
        val dlg = new Connection

        val results = dlg.typeIO((new SessionSearchRequest => [
            sortColumns = # [
                new SortColumn => [
                    descending = true
                    fieldName = "cTimestamp"
                ]
            ]
            limit = 20
        ]), ReadAllResponse).dataList
        for (dwt: results)
            println('''«(dwt as DataWithTrackingW).data»''')
    }
}
