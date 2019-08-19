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
import com.arvatosystems.t9t.base.entities.WriteTracking
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.base.search.SinkCreatedResponse
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.io.CommunicationTargetChannelType
import com.arvatosystems.t9t.io.DataSinkCategoryType
import com.arvatosystems.t9t.io.DataSinkCategoryXType
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.DataSinkKey
import com.arvatosystems.t9t.io.DataSinkRef
import com.arvatosystems.t9t.io.SinkDTO
import com.arvatosystems.t9t.io.T9tIOException
import com.arvatosystems.t9t.io.request.DataSinkSearchRequest
import com.arvatosystems.t9t.io.request.SinkSearchRequest
import com.arvatosystems.t9t.io.request.StoreSinkRequest
import de.jpaw.bonaparte.pojos.api.AsciiFilter
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

/** I/O related tests. */
class OutputSessionExceptionHandlingTest {

    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def public void outputExceptionTest() {
        dlg.errIO((new DataSinkSearchRequest => [
            searchOutputTarget = new OutputSessionParameters => [
                dataSinkId = "invalidSink"
            ]
            validate
        ]), T9tIOException.MISSING_CONFIGURATION)
    }

    @Ignore  // referential integrity error
    @Test
    def public void storeSingleSinkTest() {

        // create a new SinkDTO as required for Camel imports and store it
        val sinkDTO = new SinkDTO => [
            dataSinkRef             = new DataSinkRef(35559835983493L)  // just for test
            commTargetChannelType   = CommunicationTargetChannelType.CAMEL;
            commFormatType          = MediaXType.of(MediaType.CSV);
            fileOrQueueName         = "worldsListOfLlamas.csv";
        ]
        dlg.typeIO(new StoreSinkRequest(sinkDTO), SinkCreatedResponse)
    }

    @Test
    def public void storeSinksAndRetrieveTest() {

        // create a new SinkDTO as required for Camel imports and store it
        new DataSinkDTO => [
            dataSinkId              = "BISONs"
            commTargetChannelType   = CommunicationTargetChannelType.CAMEL;
            commFormatType          = MediaXType.of(MediaType.CSV);
            fileOrQueueNamePattern  = "Bisons${now}.csv"
            category                = DataSinkCategoryXType.of(DataSinkCategoryType.DATA_EXPORT)
            validate
            merge(dlg)
        ]
        for (var int i = 0; i < 10; i += 1) {
            // create a new SinkDTO as required for Camel imports and store it
            val ii = i    // for lambda
            val sinkDTO = new SinkDTO => [
                dataSinkRef             = new DataSinkKey("BISONs")  // just for test
                commTargetChannelType   = CommunicationTargetChannelType.CAMEL;
                commFormatType          = MediaXType.of(MediaType.CSV);
                fileOrQueueName         = '''Bisons«ii + 1».csv'''
            ]
            dlg.typeIO(new StoreSinkRequest(sinkDTO), SinkCreatedResponse)
        }
    }

    @Test
    def public void sinksRetrieveTest() {
        // search by name pattern
        val sinks = dlg.typeIO(new SinkSearchRequest => [
            searchFilter = new AsciiFilter => [
                fieldName   = "fileOrQueueName"
                likeValue   = "Bison%"
            ]
        ], ReadAllResponse) as ReadAllResponse<SinkDTO, WriteTracking>
        println('''Sinks are «sinks.dataList.map[data.fileOrQueueName].join("\n")»''')

        // search by data sink (child entity ref)
        val sinks2 = dlg.typeIO(new SinkSearchRequest => [
            searchFilter = new AsciiFilter => [
                fieldName   = "dataSink.dataSinkId"
                equalsValue = "BISONs"
            ]
        ], ReadAllResponse) as ReadAllResponse<SinkDTO, WriteTracking>
        println('''Sinks are «sinks2.dataList.map[data.fileOrQueueName].join("\n")»''')
    }
}
