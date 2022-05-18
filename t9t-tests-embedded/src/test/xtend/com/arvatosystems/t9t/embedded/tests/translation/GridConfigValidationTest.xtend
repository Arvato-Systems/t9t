/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.embedded.tests.translation

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.init.InitContainers
import de.jpaw.annotations.AddLogger
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

@AddLogger
class GridConfigValidationTest {
    static ITestConnection dlg

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void gridConfigTest() {
        val errors = InitContainers.errorCount
        if (errors !== 0) {
            LOGGER.error("There are {} grid config errors", errors)
            throw new RuntimeException("Grid config is not correct")
        } else {
            LOGGER.info("Grid config validated and OK")
        }
    }
}
