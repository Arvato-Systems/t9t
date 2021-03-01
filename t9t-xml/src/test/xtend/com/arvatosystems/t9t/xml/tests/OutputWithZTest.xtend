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
package com.arvatosystems.t9t.xml.tests

import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorXml
import com.arvatosystems.t9t.out.be.impl.formatgenerator.StandardT9tNamespaceWriter
import com.arvatosystems.t9t.out.be.impl.output.OutputResourceInMemory
import com.arvatosystems.t9t.xml.User001
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.dp.Jdp
import java.nio.charset.StandardCharsets
import org.junit.Assert
import org.junit.Test

import static extension com.arvatosystems.t9t.base.MessagingUtil.*

@AddLogger
class OutputWithZTest {
    /** Defines where the API classes sit in the Java implementation. */
    public static final String T9T_BASE_PACKAGE = "com.arvatosystems.t9t.xml";

    public static final String JAXB_CONTEXT_PATH = "de.jpaw.bonaparte.xml:" + T9T_BASE_PACKAGE;

    val myDataSink          = new DataSinkDTO => [
        dataSinkId          = "test"
        jaxbContextPath     = JAXB_CONTEXT_PATH
        xmlDefaultNamespace = "http://arvatosystems.com/schema/t9t_xml.xsd"     // default namespace
        xmlRootElementName  = "UserMaster"
        xmlRecordName       = "records"
        xmlNamespacePrefix  = "t9t_xml"
    ]
    val myOutputSessionParameters = new OutputSessionParameters => [
    ]
    val myUser = new User001 => [
        userId              = 'testUser22'
        name                = 'Test user number 22'
        isActive            = true
        emailAddress        = 'test@supertest.de'
        isTechnical         = Boolean.FALSE
        externalAuth        = Boolean.TRUE
        z                   = #{ "XYZ" -> true }
    ]

    @Test
    def void testXml() {
        Jdp.bindInstanceTo(new StandardT9tNamespaceWriter, IStandardNamespaceWriter)
        val type = MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_XML)
        val iors = new OutputResourceInMemory
        iors.open(myDataSink, myOutputSessionParameters, 1L, "dummy", type, StandardCharsets.UTF_8)

        val xml = new FormatGeneratorXml
        xml.open(myDataSink, myOutputSessionParameters, type.mediaType, null, iors, StandardCharsets.UTF_8, "ACME")
        xml.generateData(1, 1, 1234L, myUser)
        xml.close

        println(iors)
        val expected = '''
            <?xml version="1.0" ?>
            <UserMaster xmlns="http://arvatosystems.com/schema/t9t_xml.xsd" xmlns:bon="http://www.jpaw.de/schema/bonaparte.xsd">
            <t9t_xml:records xmlns:t9t_xml="http://arvatosystems.com/schema/t9t_xml.xsd"><t9t_xml:userId>testUser22</t9t_xml:userId><t9t_xml:name>Test user number 22</t9t_xml:name><t9t_xml:emailAddress>test@supertest.de</t9t_xml:emailAddress><t9t_xml:isActive>true</t9t_xml:isActive><t9t_xml:isTechnical>false</t9t_xml:isTechnical><t9t_xml:externalAuth>true</t9t_xml:externalAuth><t9t_xml:z><bon:kvp><bon:key>XYZ</bon:key><bon:bool>true</bon:bool></bon:kvp></t9t_xml:z></t9t_xml:records>
            </UserMaster>
        '''
        Assert.assertEquals(expected.normalizeEOLs, iors.toString.normalizeEOLs)
    }
}
