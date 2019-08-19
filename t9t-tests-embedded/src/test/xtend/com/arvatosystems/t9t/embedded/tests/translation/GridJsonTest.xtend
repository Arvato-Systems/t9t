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
package com.arvatosystems.t9t.embedded.tests.translation

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.uiprefs.request.GridConfigRequest
import com.arvatosystems.t9t.uiprefs.request.GridConfigResponse
import de.jpaw.bonaparte.core.JsonComposer
import de.jpaw.bonaparte.util.ToStringHelper
import org.junit.BeforeClass
import org.junit.Test

class GridJsonTest {

    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def public void gridHeaderJsonTest() {
        val rq = new GridConfigRequest => [
            gridId                      = "sliceTracking";
            selection                   = 0;
            translateInvisibleHeaders   = false
            noFallbackLanguages         = true
            overrideLanguage            = "en"
            validate
        ]
        val gridCfg = dlg.typeIO(rq, GridConfigResponse)

        println(ToStringHelper.toStringML(gridCfg.headers))
        println(JsonComposer.toJsonString(gridCfg))
    }
}
