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

import com.arvatosystems.t9t.auth.request.AuthModuleCfgSearchRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.doc.request.DocModuleCfgSearchRequest
import com.arvatosystems.t9t.email.request.EmailModuleCfgSearchRequest
import com.arvatosystems.t9t.remote.connect.Connection
import com.arvatosystems.t9t.solr.request.SolrModuleCfgSearchRequest
import de.jpaw.bonaparte.util.ToStringHelper
import org.junit.BeforeClass
import org.junit.Test

class ITModuleConfigRead {
    static ITestConnection dlg

    @BeforeClass
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new Connection
    }

    // email module config search request
    @Test
    def void readEmailModuleConfigTest() {
        val rq = new EmailModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        println('''EmailCfg result is «ToStringHelper.toStringML(resp.dataList)»''')
    }

    @Test
    def void readEmailModuleConfigWithLimitTest() {
        val rq = new EmailModuleCfgSearchRequest => [ limit = 26 ]
        val resp = dlg.typeIO(rq, ReadAllResponse)
        println('''EmailCfg result is «ToStringHelper.toStringML(resp.dataList)»''')
    }

    // doc module config search request
    @Test
    def void readDocModuleConfigTest() {
        val rq = new DocModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        println('''DocModule result is «ToStringHelper.toStringML(resp.dataList)»''')
    }

    // auth module config search request
    @Test
    def void readAuthModuleConfigTest() {
        val rq = new AuthModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        println('''Auth Module Cfg result is «ToStringHelper.toStringML(resp.dataList)»''')
    }

    // SOLR module config search request
    @Test
    def void readSolrModuleConfigTest() {
        val rq = new SolrModuleCfgSearchRequest
        val resp = dlg.typeIO(rq, ReadAllResponse)
        println('''SOLR module config result is «ToStringHelper.toStringML(resp.dataList)»''')
    }
}
