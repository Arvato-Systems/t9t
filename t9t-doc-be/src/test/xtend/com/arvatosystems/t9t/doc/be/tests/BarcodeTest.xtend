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

import com.arvatosystems.t9t.barcode.be.impl.BarcodeEan13Generator
import com.arvatosystems.t9t.barcode.be.impl.BarcodeQRCodeGenerator
import com.arvatosystems.t9t.doc.DocModuleCfgDTO
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.doc.be.converters.impl.ConverterToHtml
import com.arvatosystems.t9t.doc.be.impl.DocFormatter
import com.arvatosystems.t9t.doc.services.IDocComponentConverter
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess
import com.arvatosystems.t9t.doc.services.IImageGenerator
import com.google.common.collect.ImmutableMap
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.dp.Jdp
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static extension de.jpaw.dp.JdpExtensions.*
import org.junit.Ignore

class BarcodeTest {

    // mock the persistence access
    private static class MockedPersistenceAccess implements IDocPersistenceAccess {

        override getDocComponents(DocModuleCfgDTO cfg, DocumentSelector selector) {
            return ImmutableMap.of
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
                template  = '''
                    <html>
                        <body>
                            EAN 13:
                            <p/>
                            ${i("EAN_13", "4003994155486", 0, 30)},
                            <p/>
                            QR-Code:<p/>
                            <p/>
                            ${i("QR_CODE", "The quick brown fox jumps over the lazy dog", 254, 254)},
                        </body>
                    </html>
                '''
            ]
        }
    }

    @BeforeClass
    def static void setup() {
        Jdp.reset
        IDocModuleCfgDtoResolver.isNow(new MockedDocModuleCfgDtoResolver)
        IDocPersistenceAccess   .isNow(new MockedPersistenceAccess)
        IImageGenerator         .isNow(new BarcodeQRCodeGenerator, "QR_CODE")
        IImageGenerator         .isNow(new BarcodeEan13Generator,  "EAN_13")
        IDocComponentConverter  .isNow(new ConverterToHtml, "HTML")
    }

    @Before
    def void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    @Ignore  // currently fails with Java 8 and 10, but with different results! Generated base64 is 4 times as long in Java 10
    @Test
    def void testBarcodes() {
        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, #{
            "name" -> "Meyer",
            "amount" -> 422.78BD
        }, null)
        val expected = '''
            <html>
                <body>
                    EAN 13:
                    <p/>
                    <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGIAAAAeCAIAAABYJ1zjAAAAaklEQVR42u3QWwrAMAgEQO9/aQspLaJN6AFmP0LIY5GJzIyId70TK/MqntT95/da0k52bXNtLw+THArnGLtps6Q3Y8KECRMmTJgwYcKECRMmTJgwYcKECRMmTJgwYcKECRMmTJgwYfrLdAEpqSnGaQ9XwwAAAABJRU5ErkJggg=="/>,
                    <p/>
                    QR-Code:<p/>
                    <p/>
                    <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAP4AAAD+CAIAAACV9C8GAAAEVklEQVR42u3bu3HEMBBEQeaftJSALBV3dgD2s6/uAzRobOGeH+mTPZZA6EvoS+hL6EvoS+hL6EvoS+hL6EvoS+hL6EvoS+hL6EvoS+hL6EvoS+hL6EvoC30JfQl9CX0JfQl9CX0JfQl9CX0JfQl9CX0JfQl96TL6z2pz3+d/7/zWGt6xF+ijjz766KOPPvroo48++uijj/659E/8rLbt/98737EX6KOPPvroo48++uijjz766KOPfif9ufHi3Dec+xVvHcUT9wJ99NFHH3300UcfffTRRx999NFH/zuwvrMX6KOPPvroo48++uijjz766KOPPvpz9N8i2z/GRR999NFHH3300UcfffTRRx999NFPLkHb9bW5Y3brXqCPPvroo48++uijjz766KOPPvqn0E+2C73/Nbt7gT766KOPPvroo48++uijjz766J9Cv63ktbPkULKHWt2OWwL00UcfffTRRx999NFHH3300V+i1va3vbc47o4FXV9DH3300UcfffTRRx999NFHH330k0uwC333MCRhJR8x6KOPPvroo48++uijjz766KOP/rn0k9vfNhLt/1Pl3Mqjjz766KOPPvroo48++uijjz76dw83kxfa2g757uDym7uDPvroo48++uijjz766KOPPvroN2//E6z/2LddO0MfffTRRx999NFHH3300UcfffS/Rj+5bbsck9fXdh8N6KOPPvroo48++uijjz766KOP/t3058aLu8PNuWteyW9ouIk++uijjz766KOPPvroo48++ujvHphdjm2vST4+0EcfffTRRx999NFHH3300UcffcPNudHhHKO3ftfu6LDt4KGPPvroo48++uijjz766KOPPvrn0t8dQbZRm1vD5KOqaFCOPvroo48++uijjz766KOPPvroL9E/8drZ7iWzJHTX19BHH3300UcfffTRRx999NFHH/3da14nHsW51TidNfroo48++uijjz766KOPPvroo99Mf27kN0e/7bcnM9xEH3300UcfffTRRx999NFHH/276deNuoKjzORgd+5CW/8IG3300UcfffTRRx99oY8++uij30ltdwS5+5rdsenuAUYfffTRRx999NFHH3300UcfffRPoX/iZ7VRm+N4+mU19NFHH3300UcfffTRRx999NFHf4vj3JWytzZpbpA69z7JQ4U++uijjz766KOPPvroo48++uij30Y/yTF5YL457kQfffTRRx999NFHH3300UcfffTvo59EnLx2NneA234X+uijjz766KOPPvroo48++uij30m//5glD1X/mNJwE3300UcfffTRRx999NFHH3300U82h293CNg2Dm4OffTRRx999NFHH3300UcfffTRl9CX0JfQl9CX0JfQl9CX0JfQl9CX0JfQl9CX0JfQl9CX0JfQF/oS+hL6EvoS+hL6EvoS+hL6EvoS+hL6EvoS+hL6EvoS+hL6EvoS+hL60p/9AoLCAUvRnDEHAAAAAElFTkSuQmCC"/>,
                </body>
            </html>
        '''
        println(actual.text)
        println('''Length of expected is «expected.length», length of actual is «actual.text.length»''')
        Assert.assertEquals(expected, actual.text)
    }
}
