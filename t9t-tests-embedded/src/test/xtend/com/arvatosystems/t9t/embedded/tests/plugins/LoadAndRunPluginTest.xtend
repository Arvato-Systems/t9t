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
package com.arvatosystems.t9t.embedded.tests.plugins

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Request
import com.arvatosystems.t9t.plugins.request.UnloadPluginRequest
import com.arvatosystems.t9t.plugins.request.UploadPluginRequest
import com.arvatosystems.t9t.plugins.request.UploadPluginResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaDataUtil
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled

@AddLogger
class LoadAndRunPluginTest {

    static ITestConnection dlg

    @BeforeAll
    def static void createConnection() {
        dlg = new InMemoryConnection
    }

    @Disabled  // needs enabled plugins in special Config.XML
    @Test
    def void loadPluginTest() {
        val rq = new UploadPluginRequest => [
            description = "Demo-plugin 76"
            jarFile = MediaDataUtil.getBinaryResource("/t9t-demo-plugin.xjar")
        ]
        val rs = dlg.typeIO(rq, UploadPluginResponse)
        LOGGER.info("Result is {}", rs.pluginInfo)

        val rq2 = new ExecutePluginV1Request => [
            qualifier      = "demo"
            numParameter   = 42L
            textParameter  = "Hello, plugged world!"
            validate
        ]
        dlg.okIO(rq2)

        dlg.okIO(new UnloadPluginRequest("demo"))
        dlg.okIO(new UnloadPluginRequest("demo"))
    }

//    @Test
//    def void runPluginTest() {
//        val lpd = new LoadedPluginDTO => [
//            isActive = true
//            priority = 0
//            pluginId = "bla"
//            pluginVersion = "1.0"
//            description = "test plugin"
//            whenLoaded = Instant.now
//            jarFile = MediaDataUtil.getBinaryResource("/t9t-demo-plugin.jar")
//            validate
//        ]
//        val rq1 = new LoadedPluginCrudRequest => [
//            crud   = OperationType.CREATE
//            data = lpd
//            //naturalKey = new LoadedPluginKey(lpd.pluginId)
//        ]
//        dlg.okIO(rq1)
//
//        val rq = new ExecutePluginV1Request => [
//            qualifier      = "demo"
//            numParameter   = 42L
//            textParameter  = "Hello, plugged world!"
//            validate
//        ]
//        dlg.okIO(rq)
//    }
}
