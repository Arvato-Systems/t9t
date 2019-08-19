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
package com.arvatosystems.t9t.doc.be.tests

import com.arvatosystems.t9t.doc.DocModuleCfgDTO
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.doc.be.impl.DocFormatter
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess
import com.google.common.collect.ImmutableMap
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Jdp
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class DebugTest {

    // mock the persistence access
    private static class MockedPersistenceAccess implements IDocPersistenceAccess {

        override getDocComponents(DocModuleCfgDTO cfg, DocumentSelector selector) {
            return ImmutableMap.of("greeting", new MediaData => [
                mediaType = MediaTypes.MEDIA_XTYPE_TEXT
                text = #{
                    "de" -> "Mit freundlichen Grüßen",
                    "en" -> "Best regards"
                }.get(selector.languageCode)
            ])
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
                template = #{
                    "de" ->
                        '''
                            Sehr geehrter Herr ${d.name},

                            Das ist ${debug("6363636", 12, 87)}, jaja
                        '''
                }.get(selector.languageCode)
            ]
        }
    }

    @BeforeClass
    def public static void setup() {
        Jdp.reset
        Jdp.bindInstanceTo(new MockedDocModuleCfgDtoResolver, IDocModuleCfgDtoResolver)
        Jdp.bindInstanceTo(new MockedPersistenceAccess, IDocPersistenceAccess)
    }

    @Before
    def public void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    @Test
    def public void testSimpleDocFormatterDE() {
        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "name" -> "Meyer",
            "amount" -> 422.78BD
        }, null)
        val expected = '''
            Sehr geehrter Herr Meyer,

            Das ist debug, jaja
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }
}
