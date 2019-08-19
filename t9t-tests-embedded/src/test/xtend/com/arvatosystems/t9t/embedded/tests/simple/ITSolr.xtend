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

import com.arvatosystems.t9t.base.search.GenericTextSearchRequest
import com.arvatosystems.t9t.base.search.GenericTextSearchResponse
import com.arvatosystems.t9t.embedded.connect.Connection
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import java.util.UUID
import org.junit.Test

class ITSolr {
    private static final UUID MOON_UUID = UUID.fromString("f3b8d497-529c-487f-b788-21b80c622d12")

    @Test
    def public void searchCustomersGenericTest() {
        val dlg = new Connection(MOON_UUID)

        val resp = dlg.typeIO(new GenericTextSearchRequest => [
            searchFilter    = new UnicodeFilter => [
                fieldName   = "firstName"
                equalsValue = "Gabi"
            ]
            documentName    = "customers"
            resultFieldName = "customerRef"
        ], GenericTextSearchResponse)

        println('''received «resp.results.size» responses''')
        println('''responses are «resp.results.map[toString].join(", ")»''')
    }
}
