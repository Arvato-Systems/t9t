package com.arvatosystems.t9t.embedded.tests.io

import com.arvatosystems.t9t.all.request.UserExportRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.search.SinkCreatedResponse
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.io.CommunicationTargetChannelType
import com.arvatosystems.t9t.io.DataSinkCategoryType
import com.arvatosystems.t9t.io.DataSinkCategoryXType
import com.arvatosystems.t9t.io.DataSinkDTO
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import org.junit.BeforeClass
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

@AddLogger
class ExportUsersTest {
    static ITestConnection dlg

    @BeforeClass
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void exportUsersTest() {
        LOGGER.info("Creating DataSinkDTO ******************************")
        // create a new DataSinkDTO
        new DataSinkDTO => [
            dataSinkId              = "xmlUserExport"
            isActive                = true
            description             = "User export test"
            commTargetChannelType   = CommunicationTargetChannelType.FILE;
            commFormatType          = MediaXType.of(MediaType.XML);
            category                = DataSinkCategoryXType.of(DataSinkCategoryType.DATA_EXPORT)
            unwrapTracking          = true
            fileOrQueueNamePattern  = "users.xml";
            preTransformerName      = "xmlUserExport"
            jaxbContextPath         = "com.arvatosystems.t9t.xml"
            xmlDefaultNamespace     = "http://arvatosystems.com/schema/t9t_config.xsd"     // default namespace
            xmlRootElementName      = "UserMaster"
            xmlRecordName           = "records"
            xmlNamespacePrefix      = "t9t_config"
            validate
            merge(dlg)
        ]
        
        LOGGER.info("Exporting Users ******************************")
        // export the default user
        val resp = dlg.typeIO(new UserExportRequest, SinkCreatedResponse)
        LOGGER.info("Done, count is {} ******************************", resp.numResults)
    }
}