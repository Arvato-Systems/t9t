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
package com.arvatosystems.t9t.xml.tests;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter;
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorXml;
import com.arvatosystems.t9t.out.be.impl.formatgenerator.StandardT9tNamespaceWriter;
import com.arvatosystems.t9t.out.be.impl.output.OutputResourceInMemory;
import com.arvatosystems.t9t.xml.User001;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;

public class OutputWithZTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputWithZTest.class);

    private final DataSinkDTO myDataSink = getDataSinkDTO();
    private final OutputSessionParameters myOutputSessionParameters = new OutputSessionParameters();
    private final User001 myUser = getUser001();

    @Test
    public void testXml() throws Exception {
        StandardT9tNamespaceWriter standardT9tNamespaceWriter = new StandardT9tNamespaceWriter();
        Jdp.<IStandardNamespaceWriter>bindInstanceTo(standardT9tNamespaceWriter, IStandardNamespaceWriter.class);

        MediaTypeDescriptor type = MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_XML);
        OutputResourceInMemory iors = new OutputResourceInMemory();
        iors.open(this.myDataSink, this.myOutputSessionParameters, Long.valueOf(1L), "dummy", type, StandardCharsets.UTF_8);

        FormatGeneratorXml xml = new FormatGeneratorXml();
        xml.open(this.myDataSink, this.myOutputSessionParameters, type.getMediaType(), null, iors, StandardCharsets.UTF_8, "ACME");
        xml.generateData(1, 1, 1234L, IOutputSession.NO_PARTITION_KEY, IOutputSession.NO_RECORD_KEY, this.myUser);
        xml.close();

        LOGGER.info("Output is {}", iors);

        StringConcatenation builder = new StringConcatenation();
        builder.append("<?xml version=\"1.0\" ?>");
        builder.newLine();
        builder.append("<UserMaster xmlns=\"http://arvatosystems.com/schema/t9t_xml.xsd\" ");
        builder.append(StandardT9tNamespaceWriter.T9T_NAMESPACES);
        builder.append(">");
        builder.newLineIfNotEmpty();
        builder.append("<t9t_xml:records><t9t_xml:userId>testUser22</t9t_xml:userId><t9t_xml:name>Test user number 22</t9t_xml:name><t9t_xml:emailAddress>test@supertest.de</t9t_xml:emailAddress><t9t_xml:isActive>true</t9t_xml:isActive><t9t_xml:isTechnical>false</t9t_xml:isTechnical><t9t_xml:externalAuth>true</t9t_xml:externalAuth><t9t_xml:z><bon:kvp><bon:key>XYZ</bon:key><bon:bool>true</bon:bool></bon:kvp></t9t_xml:z></t9t_xml:records>");
        builder.newLine();
        builder.append("</UserMaster>");

        String expected = builder.toString();
        Assertions.assertEquals(MessagingUtil.normalizeEOLs(expected), MessagingUtil.normalizeEOLs(iors.toString()));
    }

    private DataSinkDTO getDataSinkDTO() {
        DataSinkDTO dataSinkDTO = new DataSinkDTO();
        dataSinkDTO.setDataSinkId("test");
        dataSinkDTO.setJaxbContextPath(StandardT9tNamespaceWriter.T9T_JAXB_PATH);
        dataSinkDTO.setXmlDefaultNamespace("http://arvatosystems.com/schema/t9t_xml.xsd");
        dataSinkDTO.setXmlRootElementName("UserMaster");
        dataSinkDTO.setXmlRecordName("records");
        dataSinkDTO.setXmlNamespacePrefix("t9t_xml");
        return dataSinkDTO;
    }

    private User001 getUser001() {
        Map<String, Object> z = new HashMap<>();
        z.put("XYZ", true);

        User001 user001 = new User001();
        user001.setUserId("testUser22");
        user001.setName("Test user number 22");
        user001.setIsActive(true);
        user001.setEmailAddress("test@supertest.de");
        user001.setIsTechnical(Boolean.FALSE);
        user001.setExternalAuth(Boolean.TRUE);
        user001.setZ(z);
        return user001;
    }
}
