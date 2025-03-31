/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.xml.User001;
import com.arvatosystems.t9t.xml.UserMaster;

import de.jpaw.bonaparte.api.media.MediaDataUtil;

/**
 * Imports a user from an XML file.
 * Demonstrates the mapping of XML key/value pairs (kvp structure) into a JSON map in Bonaparte.
 */
public class ImportWithZTest {
    @Test
    public void testImportUserWithZ() throws Exception {
        String sourceXml = MediaDataUtil.getTextResource("userTest.xml", StandardCharsets.UTF_8);
        JAXBContext context = JAXBContext.newInstance("com.arvatosystems.t9t.xml");
        Unmarshaller m = context.createUnmarshaller();

        m.setSchema(null);

        byte[] bytes = sourceXml.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream srcStream = new ByteArrayInputStream(bytes);
        Object result = m.unmarshal(srcStream);

        Assertions.assertNotNull(result, "XML parse result should not be null");
        Assertions.assertEquals(UserMaster.class, result.getClass(), "Result should be of type UserMaster");

        UserMaster pm = ((UserMaster) result);

        Assertions.assertEquals(1, pm.getRecords().size(), "Result should hold 1 user record");

        User001 sku = pm.getRecords().get(0);

        Assertions.assertNotNull(sku.getZ(), "User should have a z field");
        Assertions.assertEquals(true, sku.getZ().get("XYZ"), "XYZ should be true");
    }
}
