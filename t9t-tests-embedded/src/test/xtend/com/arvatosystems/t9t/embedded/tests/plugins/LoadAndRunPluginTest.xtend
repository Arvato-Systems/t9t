package com.arvatosystems.t9t.embedded.tests.plugins

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.plugins.LoadedPluginDTO
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Request
import com.arvatosystems.t9t.plugins.request.LoadedPluginCrudRequest
import com.arvatosystems.t9t.plugins.request.UploadPluginRequest
import com.arvatosystems.t9t.plugins.request.UploadPluginResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaDataUtil
import de.jpaw.bonaparte.pojos.api.OperationType
import org.joda.time.Instant
import org.junit.BeforeClass
import org.junit.Test
import com.arvatosystems.t9t.plugins.request.UnloadPluginRequest

@AddLogger
class LoadAndRunPluginTest {

    static ITestConnection dlg

    @BeforeClass
    def static void createConnection() {
        dlg = new InMemoryConnection
    }

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