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
import com.arvatosystems.t9t.translation.services.ITranslationProvider
import de.jpaw.bonaparte.enums.BonaTokenizableEnum
import java.util.List
import java.util.Locale

/** This test documents the use of the p and q translations functions to access property translations. */
class FormatDocumentTranslateTest {

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
                            q translates to ${q(d.value, 't9t.base.api.RetryAdviceType')}!
                            p translates simple like ${p.t9t}!
                            p needs dot replacement as in ${p.t9t$base$api$RetryAdviceType}!
                            p translates like ${("p.t9t$base$api$RetryAdviceType$" + d.value)?eval}!
                        '''
                }.get(selector.languageCode)
            ]
        }
    }

    private static class MockedTranslationProvider implements ITranslationProvider {
        override <T extends BonaTokenizableEnum> getEnumTranslation(T enu, String tenantId, String language) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }
        override getEnumTranslation(String tenantId, String enumPQON, String language, boolean tryFallbackLanguages) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }
        override getEnumTranslation(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, String fieldName) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }
        override getEnumTranslations(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, List<String> fieldNames) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }
        override getHeaderTranslation(String tenantId, String language, boolean tryFallbackLanguages, String gridId, String fieldName) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }
        override getHeaderTranslations(String tenantId, String language, boolean tryFallbackLanguages, String gridId, List<String> fieldNames) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }
        override getReportTranslation(String tenantId, Locale locale, String reportId, String fieldName) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }
        override getReportTranslation(String tenantId, String language, boolean tryFallbackLanguages, String reportId, String fieldName) {
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
        }

        override getTranslation(String tenantId, String[] langs, String path, String fieldname) {
            return '''XXX «path» XXX «fieldname» XXX'''
        }

        override resolveLanguagesToCheck(String language, boolean tryFallbackLanguages) {
            return "de,en".split(",")
        }
    }

    @BeforeClass
    def static void setup() {
        Jdp.reset
        Jdp.bindInstanceTo(new MockedDocModuleCfgDtoResolver, IDocModuleCfgDtoResolver)
        Jdp.bindInstanceTo(new MockedPersistenceAccess, IDocPersistenceAccess)
        Jdp.bindInstanceTo(new MockedTranslationProvider, ITranslationProvider)
    }

    @Before
    def void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    @Test
    def void testSimpleDocFormatterDE() {
        val actual = new DocFormatter().formatDocument("TEST", 136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "value" -> "RETRY_ON_ERROR"
        }, null)
        val expected = '''
            q translates to XXX t9t.base.api.RetryAdviceType XXX RETRY_ON_ERROR XXX!
            p translates simple like XXX  XXX t9t XXX!
            p needs dot replacement as in XXX t9t.base.api XXX RetryAdviceType XXX!
            p translates like XXX t9t.base.api.RetryAdviceType XXX RETRY_ON_ERROR XXX!
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }
}
