/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

    def void setupDataSink(String filenamePattern) {
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
            fileOrQueueNamePattern  = filenamePattern
            preTransformerName      = "xmlUserExport"
            jaxbContextPath         = "com.arvatosystems.t9t.xml"
            xmlDefaultNamespace     = "http://arvatosystems.com/schema/t9t_config.xsd"     // default namespace
            xmlRootElementName      = "UserMaster"
            xmlRecordName           = "records"
            xmlNamespacePrefix      = "t9t_config"
            validate
            merge(dlg)
        ]
    }

    @Test
    def void exportUsersTest() {
        setupDataSink("users.xml")
        LOGGER.info("Exporting Users ******************************")
        // export the default user
        val resp = dlg.typeIO(new UserExportRequest, SinkCreatedResponse)
        LOGGER.info("Done, count is {} ******************************", resp.numResults)
    }

    @Test
    def void exportUsersSplittedTest() {
        setupDataSink("users-${partNo}.xml")
        LOGGER.info("Exporting Users ******************************")
        // export the default user
        val exportRq = new UserExportRequest => [
            maxRecords           = 1
        ]
        val resp = dlg.typeIO(exportRq, SinkCreatedResponse)
        LOGGER.info("Done, count is {} ******************************", resp.numResults)
    }
}
