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
package com.arvatosystems.t9t.out.be.tests

import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorCsv
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorJson
import com.arvatosystems.t9t.out.be.impl.output.OutputResourceInMemory
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.dp.Jdp
import java.nio.charset.StandardCharsets
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

@AddLogger
class OutputTest {

    @BeforeClass
    def static void setup() {
        Jdp.reset
        // use real implementations only if they are defined within this project and use stubs for everything else
    //    Jdp.bindInstanceTo(new ItemTaxComputerFactory, IItemTaxComputerFactory)
    }

    val myDataSink      = new DataSinkDTO => [
        dataSinkId      = "test"
    ]
    val myOutputSessionParameters = new OutputSessionParameters => [
    ]
    val myRecord = new ServiceResponse => [
        returnCode      = 4711
        errorDetails    = "bad field xyz"
        processRef      = 12L
    ]

    @Test
    def void testCsv() {
        val type = MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_CSV)
        val iors = new OutputResourceInMemory
        iors.open(myDataSink, myOutputSessionParameters, 1L, "dummy", type, StandardCharsets.UTF_8)

        val csv = new FormatGeneratorCsv
        csv.open(myDataSink, myOutputSessionParameters, type.mediaType, null, iors, StandardCharsets.UTF_8, "ACME")
        csv.generateData(1, 1, 6263636363L, myRecord)
        csv.generateData(2, 2, 6263636364L, myRecord)
        csv.close

        val expected = '''
            4711;;12;;"bad field xyz";
            4711;;12;;"bad field xyz";
        '''
        Assert.assertEquals(expected, iors.toString)
    }

    @Test
    def void testJson() {
        val type = MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_JSON)
        val iors = new OutputResourceInMemory
        iors.open(myDataSink, myOutputSessionParameters, 1L, "dummy", type, StandardCharsets.UTF_8)

        val csv = new FormatGeneratorJson
        csv.open(myDataSink, myOutputSessionParameters, type.mediaType, null, iors, StandardCharsets.UTF_8, "ACME")
        csv.generateData(1, 1, 6263636363L, myRecord)
        csv.generateData(2, 2, 6263636364L, myRecord)
        csv.close

        val expected = '''
            [{"@PQON":"t9t.base.api.ServiceResponse","returnCode":4711,"processRef":12,"errorDetails":"bad field xyz"}
            ,{"@PQON":"t9t.base.api.ServiceResponse","returnCode":4711,"processRef":12,"errorDetails":"bad field xyz"}
            ]'''
        Assert.assertEquals(expected, iors.toString)
    }
}
