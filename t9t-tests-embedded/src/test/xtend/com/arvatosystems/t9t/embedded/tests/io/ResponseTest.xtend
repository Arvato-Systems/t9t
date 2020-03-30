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
package com.arvatosystems.t9t.embedded.tests.io

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.search.SinkCreatedResponse
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.io.CommunicationTargetChannelType
import com.arvatosystems.t9t.io.DataSinkCategoryType
import com.arvatosystems.t9t.io.DataSinkCategoryXType
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.Text
import com.arvatosystems.t9t.io.request.WriteRecordsToDataSinkRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import java.util.ArrayList
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*
import static extension com.arvatosystems.t9t.auth.extensions.AuthExtensions.*

import com.arvatosystems.t9t.io.DataSinkKey
import com.arvatosystems.t9t.in.be.impl.ImportTools
import java.util.UUID
import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.PermissionsDTO
import de.jpaw.bonaparte.pojos.api.auth.Permissionset

@AddLogger
class ResponseTest {
    static ITestConnection dlg
    static val MY_ID = "responseXLS"
    static val MY_ID_IN = "requestJson"
    static final UUID DUMMY_API_KEY = UUID.fromString("beefbeef-affe-cafe-babe-2a539c291111")

    @BeforeClass
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection

        LOGGER.info("Creating DataSinkDTO ******************************")
        // create a new DataSinkDTO
        new DataSinkDTO => [
            dataSinkId              = MY_ID
            isActive                = true
            description             = "Single Response file to XLS"
            commTargetChannelType   = CommunicationTargetChannelType.FILE;
            commFormatType          = MediaXType.of(MediaType.XLSX);
            category                = DataSinkCategoryXType.of(DataSinkCategoryType.DATA_EXPORT)
            unwrapTracking          = false
            fileOrQueueNamePattern  = "response-${ref}.xlsx";
            validate
            merge(dlg)
        ]
        // create a new DataSinkDTO
        new DataSinkDTO => [
            dataSinkId              = MY_ID_IN
            isActive                = true
            description             = "Test import"
            commTargetChannelType   = CommunicationTargetChannelType.FILE;
            commFormatType          = MediaXType.of(MediaType.JSON);
            category                = DataSinkCategoryXType.of(DataSinkCategoryType.DATA_EXPORT)
            isInput                 = true
            baseClassPqon           = "t9t.io.request.ImportStatusRequest"
            fileOrQueueNamePattern  = "input.json";
            responseDataSinkRef     = new DataSinkKey(MY_ID)
            validate
            merge(dlg)
        ]
        // create some API KEY
        new ApiKeyDTO => [
            apiKey                  = DUMMY_API_KEY
            userRef                 = new UserKey("admin")
            isActive                = true
            name                    = "key for embedded test class ResponseTest"
            permissions             = new PermissionsDTO => [
                minPermissions      = new Permissionset(0xfffff)
                maxPermissions      = new Permissionset(0xfffff)
                resourceIsWildcard  = true
                resourceRestriction = "B."
            ]
            merge(dlg)
        ]
        LOGGER.info("Creating DataSinkDTO DONE ******************************")
    }

    def createRecords(String text, int num) {
        val arr = new ArrayList<BonaPortable>(num)
        for (var int i = 0; i < num; i += 1)
            arr.add(new Text(text + i))
        return arr
    }

    @Test
    def void exportResponseTest() {
        LOGGER.info("Exporting Dummy records ******************************")
        val rq = new WriteRecordsToDataSinkRequest
        rq.dataSinkId = MY_ID
        rq.records1 = createRecords("foo", 3)
        rq.records2 = createRecords("bar", 5)
        val resp = dlg.typeIO(rq, SinkCreatedResponse)
        LOGGER.info("Done, count is {} ******************************", resp.numResults)
        Assert.assertEquals("Number of written records", 8, resp.numResults)
    }

    static val JSON = '''[
        { "responses": [ { "@PQON": "t9t.io.Text", "text": "line A" } ] },
        { "responses": [ { "@PQON": "t9t.io.Text", "text": "line 2" } ] }
    ]'''

    @Test
    def void importTest() {
        LOGGER.info("Importing Dummy records ******************************")
        ImportTools.importFromString(JSON, DUMMY_API_KEY, "mytest", MY_ID_IN, #{})
        LOGGER.info("Done ******************************")
    }
}
