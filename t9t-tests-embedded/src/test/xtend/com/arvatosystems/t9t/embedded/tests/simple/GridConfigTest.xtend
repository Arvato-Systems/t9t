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

import com.arvatosystems.t9t.auth.request.AuthModuleCfgSearchRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.doc.request.DocModuleCfgSearchRequest
import com.arvatosystems.t9t.email.request.EmailModuleCfgSearchRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.solr.request.SolrModuleCfgSearchRequest
import com.arvatosystems.t9t.uiprefs.request.GridConfigRequest
import de.jpaw.bonaparte.util.ToStringHelper
import org.junit.BeforeClass
import org.junit.Test
import org.junit.Assert

class GridConfigTest {
    static ITestConnection dlg

    @BeforeClass
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void readEmailModuleConfigWithLimitTest() {
        val rq = new EmailModuleCfgSearchRequest => [ limit = 26 ]
        val resp = dlg.typeIO(rq, ReadAllResponse)
        println('''EmailCfg result is «ToStringHelper.toStringML(resp.dataList)»''')
    }

    // email module config search request
    @Test
    def void readEmailModuleConfigTest() {
        val rq = new EmailModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        Assert.assertEquals(0, resp.dataList.size)
    }

    // doc module config search request
    @Test
    def void readDocModuleConfigTest() {
        val rq = new DocModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        Assert.assertEquals(0, resp.dataList.size)
    }

    // auth module config search request
    @Test
    def void readAuthModuleConfigTest() {
        val rq = new AuthModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        Assert.assertEquals(0, resp.dataList.size)
    }

    // SOLR module config search request
    @Test
    def void readSolrModuleConfigTest() {
        val rq = new SolrModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        Assert.assertEquals(0, resp.dataList.size)
    }

    @Test
    def void configEnTest() {
        val rq = new GridConfigRequest => [
            gridId                    = "docComponent"
            translateInvisibleHeaders = true
            validate
        ]

        println(ToStringHelper.toStringML(dlg.okIO(rq)))
    }

    @Test
    def void configDeTest() {
        val rq = new GridConfigRequest => [
            gridId                    = "docConfig"
            translateInvisibleHeaders = true
            overrideLanguage          = "de"
            validate
        ]

        println(ToStringHelper.toStringML(dlg.okIO(rq)))
    }
}
