/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.request.GetQualifiersRequest
import com.arvatosystems.t9t.base.request.GetQualifiersResponse
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class GetQualifiersTest {
    static InMemoryConnection dlg

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void goodQualifierTest() {
        val result = dlg.typeIO(new GetQualifiersRequest(#[ "com.arvatosystems.t9t.out.services.IMarshallerExt" ]), GetQualifiersResponse).qualifiers;
        Assertions.assertTrue(result.size >= 2, "Expect at least 2 implementations of IMarshallerExt")
        Assertions.assertTrue(result.contains("XML"), "Expect implementation of IMarshallerExt named XML")
        Assertions.assertTrue(result.contains("JSONJackson"), "Expect implementation of IMarshallerExt named JSONJackson")
    }

    @Test
    def void noQualifiersTest() {
        val result = dlg.typeIO(new GetQualifiersRequest(#[ "com.arvatosystems.t9t.base.services.IExecutor" ]), GetQualifiersResponse);
        Assertions.assertEquals(0, result.qualifiers.size)  // only unqualified implementations exist
    }

    @Test
    def void badClassTest() {
        val result = dlg.doIO(new GetQualifiersRequest(#[ "com.arvatosystems.t9t.thereIsNoSuch.Class" ]));
        Assertions.assertEquals(T9tException.INVALID_REQUEST_PARAMETER_TYPE, result.returnCode)
    }
}
