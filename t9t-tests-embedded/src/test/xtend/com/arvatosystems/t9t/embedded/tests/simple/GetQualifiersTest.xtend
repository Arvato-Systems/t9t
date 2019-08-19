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

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.request.GetQualifiersRequest
import com.arvatosystems.t9t.base.request.GetQualifiersResponse
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class GetQualifiersTest {
    static InMemoryConnection dlg

    @BeforeClass
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void goodQualifierTest() {
        val result = dlg.typeIO(new GetQualifiersRequest(#[ "com.arvatosystems.t9t.out.services.IMarshallerExt" ]), GetQualifiersResponse);
        Assert.assertEquals(1, result.qualifiers.size)
        Assert.assertEquals("XML", result.qualifiers.iterator.next)
    }
    @Test
    def void noQualifiersTest() {
        val result = dlg.typeIO(new GetQualifiersRequest(#[ "com.arvatosystems.t9t.base.services.IExecutor" ]), GetQualifiersResponse);
        Assert.assertEquals(0, result.qualifiers.size)  // only unqualified implementations exist
    }
    @Test
    def void badClassTest() {
        val result = dlg.doIO(new GetQualifiersRequest(#[ "com.arvatosystems.t9t.thereIsNoSuch.Class" ]));
        Assert.assertEquals(T9tException.INVALID_REQUEST_PARAMETER_TYPE, result.returnCode)
    }
}
