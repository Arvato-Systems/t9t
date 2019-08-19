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

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.uiprefs.request.GridConfigRequest
import com.arvatosystems.t9t.uiprefs.request.GridConfigResponse
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigRequest
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigResponse
import de.jpaw.bonaparte.util.ToStringHelper
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class GridRequestHandlerTest {
    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    /**
     * Test of GridConfigRequetHandler should give the same result as LeanGridConfigRequestHandler
     * after FT-3217
     */
    @Test
    def public void gridRequestOnReportConfig() {
        println("Launching gridRequestOnReportConfig");

        println("Executing GridConfigRequest on GridConfigRequestHandler");
        val req = new GridConfigRequest => [
            gridId = "reportConfig";
            selection = 0;
            translateInvisibleHeaders = false
            noFallbackLanguages = true
            overrideLanguage = "en"
            validate
        ]
        val gridCfg = dlg.typeIO(req, GridConfigResponse)

        println("\nResult of GridConfigResponse: ");
        println(ToStringHelper.toStringML(gridCfg.headers))

        println("Executing GridConfigRequest on GridConfigRequestHandler");
        val leanReq = new LeanGridConfigRequest => [
            gridId = "reportConfig";
            selection = 0;
            noFallbackLanguages = true
            overrideLanguage = "en"
            validate
        ]
        val leanGridCfg = dlg.typeIO(leanReq, LeanGridConfigResponse);
        println("\n\nResult of LeanGridConfigResponse: ");
        println(ToStringHelper.toStringML(leanGridCfg.headers))

        Assert.assertEquals(gridCfg.headers, leanGridCfg.headers)
    }

    @Test
    def public void gridRequestOnReportParams() {
        println("Launching gridRequestOnReportParams");

        println("Executing GridConfigRequest on GridConfigRequestHandler");
        val req = new GridConfigRequest => [
            gridId = "reportParams";
            selection = 0;
            translateInvisibleHeaders = false
            noFallbackLanguages = true
            overrideLanguage = "en"
            validate
        ]
        val gridCfg = dlg.typeIO(req, GridConfigResponse)

        println("\nResult of GridConfigResponse: ");
        println(ToStringHelper.toStringML(gridCfg.headers))

        println("Executing GridConfigRequest on GridConfigRequestHandler");
        val leanReq = new LeanGridConfigRequest => [
            gridId = "reportParams";
            selection = 0;
            noFallbackLanguages = true
            overrideLanguage = "en"
            validate
        ]
        val leanGridCfg = dlg.typeIO(leanReq, LeanGridConfigResponse);
        println("\n\nResult of LeanGridConfigResponse: ");
        println(ToStringHelper.toStringML(leanGridCfg.headers))

        Assert.assertEquals(gridCfg.headers, leanGridCfg.headers)
    }

    @Test
    def public void gridRequestOnDocConfig() {
        println("Launching gridRequestOnDocConfig");

        println("Executing GridConfigRequest on GridConfigRequestHandler");
        val req = new GridConfigRequest => [
            gridId = "docConfig";
            selection = 0;
            translateInvisibleHeaders = false
            noFallbackLanguages = true
            overrideLanguage = "en"
            validate
        ]
        val gridCfg = dlg.typeIO(req, GridConfigResponse)

        println("\nResult of GridConfigResponse: ");
        println(ToStringHelper.toStringML(gridCfg.headers))

        println("Executing GridConfigRequest on GridConfigRequestHandler");
        val leanReq = new LeanGridConfigRequest => [
            gridId = "docConfig";
            selection = 0;
            noFallbackLanguages = true
            overrideLanguage = "en"
            validate
        ]
        val leanGridCfg = dlg.typeIO(leanReq, LeanGridConfigResponse);
        println("\n\nResult of LeanGridConfigResponse: ");
        println(ToStringHelper.toStringML(leanGridCfg.headers))

        Assert.assertEquals(gridCfg.headers, leanGridCfg.headers)
    }
}
