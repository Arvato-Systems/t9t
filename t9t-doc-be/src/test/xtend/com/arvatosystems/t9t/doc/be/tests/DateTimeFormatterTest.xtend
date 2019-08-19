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
import de.jpaw.dp.Jdp
import java.util.Locale
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/** Note: Java 10 and Java 8 differ in day and timestamp formatting (space vs - for GB, and additional commas separating day and time for timestamp. **/
class DateTimeFormatterTest {

    static char hy = ' '          // hyphen or space to separate date fields in GB ('-' for 1.8, space for 9 and 10
    static String comma = ",";    // separates time and date (empty for 1.8, comma for 9 and 10)

    def void print(String language, String country) {
        println('''Date / time format for «language», «country»''')
        val now = new LocalDateTime()
        val time = new LocalTime
        val date = new LocalDate
        val locale = new Locale(language, country)
//        val fmt = ISODateTimeFormat.basicDateTime
//        val string = fmt.print(now)
//        println('''It is now «string»''')

        val formats = #[ 'S', 'M', 'L', 'F' ]
        formats.forEach[
            val nowS  = DateTimeFormat.forStyle('''«it»«it»''').withLocale(locale).print(now);
            val dayS  = DateTimeFormat.forStyle('''«it»-''').withLocale(locale).print(date);
            val timeS = DateTimeFormat.forStyle('''-«it»''').withLocale(locale).print(time);
            println('''For modifier «it», the date is «dayS», the time «timeS», and full «nowS»''')
        ]
        println
    }


    @Test
    def void printDates() {
        print('de', 'DE')
        print('en', 'GB')
        print('en', 'US')
        print('fr', 'FR')
    }

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
                template = #{
                    "de" ->
                        '''
                            Tagesdatum: ${t(d.day, 'M-')},
                            Uhrzeit:    ${t(d.time, '-M')},
                            Datum+Zeit: ${t(d.ts, 'MM')}
                            Tagesdatum: ${t(d.ts, 'M-', 'D')},
                            Uhrzeit:    ${t(d.ts, '-M', 'T')},
                            Jahr:       ${t(d.ts, 'MM', 'I', 'YYYY')},
                            Wochentag:  ${t(d.ts, 'MM', 'I', 'EEEE MMMM')},
                        ''',
                    "en" ->
                        '''
                            Day:        ${t(d.day, 'M-')},
                            Time:       ${t(d.time, '-M')},
                            Timestamp:  ${t(d.ts, 'MM')}
                            Day:        ${t(d.ts, 'M-', 'D')},
                            Time:       ${t(d.ts, '-M', 'T')},
                            Custom:     ${t(d.ts, 'MM', 'I', 'YYYY')},
                            DoW, month: ${t(d.ts, 'MM', 'I', 'EEEE MMMM')},
                        '''
                }.get(selector.languageCode)
            ]
        }
    }

    @BeforeClass
    def static void setup() {
        Jdp.reset
        Jdp.bindInstanceTo(new MockedDocModuleCfgDtoResolver, IDocModuleCfgDtoResolver)
        Jdp.bindInstanceTo(new MockedPersistenceAccess, IDocPersistenceAccess)

        // set Java 8 / later differences
        val version = System.getProperty("java.version")
        println('''Java version is «version»''')
        if (version.startsWith("1.8")) {
            comma = ""
            hy = "-"
        }
    }

    @Before
    def void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    val data = #{
        "day"  -> new LocalDate(2016, 8, 15),
        "time" -> new LocalTime(17, 26, 58),
        "ts"   -> new LocalDateTime(2016, 8, 15, 17, 26, 58)
    }

    @Test
    def void testDateTimeDocFormatterDE() {
        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], "Europe/Berlin", data, null)
        val expected = '''
            Tagesdatum: 15.08.2016,
            Uhrzeit:    17:26:58,
            Datum+Zeit: 15.08.2016«comma» 19:26:58
            Tagesdatum: 15.08.2016,
            Uhrzeit:    17:26:58,
            Jahr:       2016,
            Wochentag:  Montag August,
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }

    @Test
    def void testDateTimeDocFormatterUTC() {
        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], null, data, null)
        val expected = '''
            Tagesdatum: 15.08.2016,
            Uhrzeit:    17:26:58,
            Datum+Zeit: 15.08.2016«comma» 17:26:58
            Tagesdatum: 15.08.2016,
            Uhrzeit:    17:26:58,
            Jahr:       2016,
            Wochentag:  Montag August,
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }

    @Test
    def void testDateTimeDocFormatterGB() {
        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "en"
            countryCode  = "GB"
            currencyCode = "GBP"
        ], null, data, null)
        val expected = '''
            Day:        15«hy»Aug«hy»2016,
            Time:       17:26:58,
            Timestamp:  15«hy»Aug«hy»2016«comma» 17:26:58
            Day:        15«hy»Aug«hy»2016,
            Time:       17:26:58,
            Custom:     2016,
            DoW, month: Monday August,
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }

    @Test
    def void testDateTimeDocFormatterUS() {
        val actual = new DocFormatter().formatDocument(136138L, TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "en"
            countryCode  = "US"
            currencyCode = "USD"
        ], null, data, null)
        val expected = '''
            Day:        Aug 15, 2016,
            Time:       5:26:58 PM,
            Timestamp:  Aug 15, 2016«comma» 5:26:58 PM
            Day:        Aug 15, 2016,
            Time:       5:26:58 PM,
            Custom:     2016,
            DoW, month: Monday August,
        '''
        println(actual.text)
        Assert.assertEquals(expected, actual.text)
    }
}
