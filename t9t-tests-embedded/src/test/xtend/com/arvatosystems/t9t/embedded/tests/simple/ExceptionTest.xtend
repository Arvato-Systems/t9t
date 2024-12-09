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
import com.arvatosystems.t9t.base.request.ExceptionRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.annotations.AddLogger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

@AddLogger
class ExceptionTest {
    static InMemoryConnection dlg

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void throwNPE() {
        val rqNPE = new ExceptionRequest
        rqNPE.specialCause = "NPE"
        val result = dlg.doIO(rqNPE);
        LOGGER.info("Result (NPE expected) is {}: {}, {}", result.returnCode, result.errorDetails, result.errorMessage)
        Assertions.assertEquals(T9tException.NULL_POINTER, result.returnCode, "NPE exception code")
    }

    @Test
    def void throwClassCast() {
        val rqCC = new ExceptionRequest
        rqCC.specialCause = "CLASSCAST"
        val result = dlg.doIO(rqCC);
        LOGGER.info("Result (ClassCast expected) is {}: {}, {}", result.returnCode, result.errorDetails, result.errorMessage)
        Assertions.assertEquals(T9tException.CLASS_CAST, result.returnCode, "Class cast exception code")
    }

    @Test
    def void throwWithDetails() {
        val rqExcDet = new ExceptionRequest
        rqExcDet.returnCode = T9tException.RECORD_DOES_NOT_EXIST
        rqExcDet.errorMessage = "myDetails"
        val result = dlg.doIO(rqExcDet);
        LOGGER.info("Result is {}: {} ({})", result.returnCode, result.errorDetails, result.errorMessage)
        Assertions.assertEquals(T9tException.RECORD_DOES_NOT_EXIST, result.returnCode, "returnCode")
        Assertions.assertEquals("myDetails", result.errorDetails, "errorDetails")
    }
}
