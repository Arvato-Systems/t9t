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

import com.arvatosystems.t9t.xml.UserMaster
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaDataUtil
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.bind.JAXBContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Imports a user from an XML file.
 * Demonstrates the mapping of XML key/value pairs (kvp structure) into a JSON map in Bonaparte.
 */
@AddLogger
class ImportWithZTest {

    @Test
    def testImportUserWithZ() {
        val sourceXml = MediaDataUtil.getTextResource("userTest.xml", StandardCharsets.UTF_8)
        val context = JAXBContext.newInstance("com.arvatosystems.t9t.xml");
        val m = context.createUnmarshaller();
        m.schema = null
        val srcStream = new ByteArrayInputStream(sourceXml.getBytes(StandardCharsets.UTF_8))
        val result = m.unmarshal(srcStream)
        Assertions.assertNotNull(result, "XML parse result should not be null")
        Assertions.assertEquals(UserMaster, result.class, "Result should be of type UserMaster")
        val pm = result as UserMaster
        Assertions.assertEquals(1, pm.records.size, "Result should hold 1 user record")
        val sku = pm.records.get(0)
        Assertions.assertNotNull(sku.z, "User should have a z field")
        Assertions.assertEquals(true, sku.z.get("XYZ"), "XYZ should be true")
    }
}
