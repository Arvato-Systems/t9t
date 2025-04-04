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
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Jdp
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.util.TimeZone

@AddLogger
class FormatDocumentTest {

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
                            sie haben in der Verlosung ${d.amount} ${e.currencySymbol} (${s.currencyCode}) gewonnen.
                            ${c.greeting}
                        ''',
                    "en" ->
                        '''
                            Dear Mr. ${d.name},
                            you've won ${d.amount} ${e.currencySymbol} (${s.currencyCode})!
                            ${c.greeting}
                        '''
                }.get(selector.languageCode)
            ]
        }
    }

    @BeforeAll
    def public static void setup() {
        val oldId = ZoneId.systemDefault().getId();
        if ("GMT" != oldId) {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            val newId = ZoneId.systemDefault().getId();
            LOGGER.info("Setting time zone to GMT (UTC) (was {} before, now changed to {})", oldId, newId);
        } else {
            LOGGER.info("Time zone already set to GMT - good");
        }
        Jdp.reset
        Jdp.bindInstanceTo(new MockedDocModuleCfgDtoResolver, IDocModuleCfgDtoResolver)
        Jdp.bindInstanceTo(new MockedPersistenceAccess, IDocPersistenceAccess)
    }

    @BeforeEach
    def public void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    @Test
    def public void testSimpleDocFormatterDE() {
        val actual = new DocFormatter().formatDocument("TEST", "TEST", TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "name" -> "Meyer",
            "amount" -> 422.78BD
        }, null)
        val expected = '''
            Sehr geehrter Herr Meyer,
            sie haben in der Verlosung 422,78 € (EUR) gewonnen.
            Mit freundlichen Grüßen
        '''
        LOGGER.info("Generated text is {}", actual.text)
        Assertions.assertEquals(expected, actual.text)
    }

    @Test
    def public void testSimpleDocFormatterGB() {
        val actual = new DocFormatter().formatDocument("TEST", "TEST", TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "en"
            countryCode  = "GB"
            currencyCode = "GBP"
        ], null, #{
            "name" -> "Meyer",
            "amount" -> 422.78BD
        }, null)
        val expected = '''
            Dear Mr. Meyer,
            you've won 422.78 £ (GBP)!
            Best regards
        '''
        LOGGER.info("Generated text is {}", actual.text)
        Assertions.assertEquals(expected, actual.text)
    }
}
