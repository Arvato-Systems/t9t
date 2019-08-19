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
import com.arvatosystems.t9t.jdp.Init
import com.google.common.collect.ImmutableMap
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Jdp
import de.jpaw.util.ByteArray
import java.nio.charset.StandardCharsets
import java.util.HashMap
import org.eclipse.xtend.lib.annotations.Data
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.joda.time.LocalDateTime

class FormatHtmlDocTest {

    // mock the persistence access
    @Data
    private static class MockedPersistenceAccess implements IDocPersistenceAccess {
        private String templateText

        override getDocComponents(DocModuleCfgDTO cfg, DocumentSelector selector) {
            val sampleBytes = "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".getBytes(StandardCharsets.UTF_8)

            return ImmutableMap.of(
                "truth",  new MediaData => [ mediaType = MediaTypes.MEDIA_XTYPE_TEXT text = "28 < 42" ],
                "markup", new MediaData => [ mediaType = MediaTypes.MEDIA_XTYPE_HTML text = "My <b>dear</b> friend" ],
                "image1", new MediaData => [ mediaType = MediaTypes.MEDIA_XTYPE_PNG  text = "http://apache.org/img/asf_logo.png" ],
                "image2", new MediaData => [
                    mediaType   = MediaTypes.MEDIA_XTYPE_PNG
                    text        = "http://apache.org/img/asf_logo.png"
                    z           = #{ "width" -> 600, "height" -> 158 }
                ],
                "image3", new MediaData => [
                    mediaType   = MediaTypes.MEDIA_XTYPE_PNG
                    rawData     = ByteArray.fromBase64(sampleBytes, 0, sampleBytes.length)
                    z           = #{ "alt" -> "Where's the bus?", "style" -> "background", "cid" -> "image3" }
                ]
            )
        }

        override getDocConfigDTO(String templateId) {
            throw new UnsupportedOperationException("Not required for this test")
        }

        override getDocEmailCfgDTO(DocModuleCfgDTO cfg, String templateId, DocumentSelector selector) {
            throw new UnsupportedOperationException("Not required for this test")
        }

        override getDocTemplateDTO(DocModuleCfgDTO cfg, String templateId, DocumentSelector selector) {
            return new DocTemplateDTO => [
                mediaType = MediaTypes.MEDIA_XTYPE_HTML
                template  = templateText
            ]
        }
    }

    @BeforeClass
    def public static void setup() {
        Jdp.reset
        Init.initializeT9t    // required to get the converters
        Jdp.bindInstanceTo(new MockedDocModuleCfgDtoResolver, IDocModuleCfgDtoResolver)
    }

    @Before
    def public void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    @Test
    def public void testSimpleHtmlFormatter() {
        Jdp.bindInstanceTo(new MockedPersistenceAccess('''
            <html>
                <body>
                    ${c.markup} ${d.name},
                    <p>
                    you should know that ${c.truth}.
                </body>
            </html>
        '''), IDocPersistenceAccess)

        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "name" -> "John",
            "amount" -> 422.78BD
        }, null)

        val expected = '''
            <html>
                <body>
                    My <b>dear</b> friend John,
                    <p>
                    you should know that 28 &lt; 42.
                </body>
            </html>
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }

    @Test
    def public void testHtmlFormatterWithImages() {
        Jdp.bindInstanceTo(new MockedPersistenceAccess('''
            <html>
                <body>
                    ${c.image1}
                    <p>
                    ${c.image2}
                    <p>
                    ${c.image3}
                </body>
            </html>
        '''), IDocPersistenceAccess)

        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "name" -> "John",
            "amount" -> 422.78BD
        }, null)

        val expected = '''
            <html>
                <body>
                    <img src="http://apache.org/img/asf_logo.png"/>
                    <p>
                    <img src="http://apache.org/img/asf_logo.png" width="600px" height="158px"/>
                    <p>
                    <img src="data:image/png;base64,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" alt="Where&#39;s the bus?" style="background"/>
                </body>
            </html>
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }

    @Test
    def public void testHtmlFormatterWithImagesUsingCid() {
        val attachments = new HashMap<String, MediaData>(10)
        Jdp.bindInstanceTo(new MockedPersistenceAccess('''
            <html>
                <body>
                    ${c.image1}
                    <p>
                    ${c.image2}
                    <p>
                    ${c.image3}
                </body>
            </html>
        '''), IDocPersistenceAccess)

        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "name" -> "John",
            "amount" -> 422.78BD
        }, attachments)

        val expected = '''
            <html>
                <body>
                    <img src="http://apache.org/img/asf_logo.png"/>
                    <p>
                    <img src="http://apache.org/img/asf_logo.png" width="600px" height="158px"/>
                    <p>
                    <img src="cid:image3" alt="Where&#39;s the bus?" style="background"/>
                </body>
            </html>
        '''
        println(actual.text)
        Assert.assertEquals(1, attachments.size)
        Assert.assertEquals(expected, actual.text)
    }

    @Test
    def public void testHtmlFormatterWithDateMethod() {
        Jdp.bindInstanceTo(new MockedPersistenceAccess('''
            <html>
                <body>
                    Hello ${d.name} on ${t(d.orderDate, 'M-', 'D')}
                </body>
            </html>
        '''), IDocPersistenceAccess)

        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "name" -> "John",
            "orderDate" -> new LocalDateTime(2018,12,14,9,27,0)
        }, null)

        val expected = '''
            <html>
                <body>
                    Hello John on 14.12.2018
                </body>
            </html>
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }
}
