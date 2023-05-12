/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.doc.be.tests

import com.arvatosystems.t9t.doc.DocModuleCfgDTO
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.doc.be.impl.DocFormatter
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess
import com.google.common.collect.ImmutableMap
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.dp.Jdp
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@AddLogger
class EncodingTest {

    // mock the persistence access
    private static class MockedPersistenceAccess implements IDocPersistenceAccess {

        override getDocComponents(DocModuleCfgDTO cfg, DocumentSelector selector) {
            return ImmutableMap.of()
        }

        override getDocConfigDTO(String templateId) {
            throw new UnsupportedOperationException("Not required for this test")
        }

        override getDocEmailCfgDTO(DocModuleCfgDTO cfg, String templateId, DocumentSelector selector) {
            throw new UnsupportedOperationException("Not required for this test")
        }

        override getDocTemplateDTO(DocModuleCfgDTO cfg, String templateId, DocumentSelector selector) {
            return new DocTemplateDTO => [
                mediaType = MediaTypes.MEDIA_XTYPE_TEXT
                template = "This is encoded: ${base64(d.text)}!"
            ]
        }
    }

    @BeforeAll
    def static void setup() {
        Jdp.reset
        Jdp.bindInstanceTo(new MockedDocModuleCfgDtoResolver, IDocModuleCfgDtoResolver)
        Jdp.bindInstanceTo(new MockedPersistenceAccess, IDocPersistenceAccess)
    }

    @BeforeEach
    def void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    val data = #{
        "text"  -> "a+b@x.com"
    }

    @Test
    def void runTest() {
        val actual = new DocFormatter().formatDocument("TEST", "TEST", TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], "Europe/Berlin", data, null)
        val expected = "This is encoded: YStiQHguY29t!"
        Assertions.assertEquals(expected, actual.text)
    }
}
