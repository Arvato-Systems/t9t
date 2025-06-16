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
import de.jpaw.dp.Jdp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static java.time.format.FormatStyle.*
import com.arvatosystems.t9t.doc.DocConstants

/** Note: Java 10 and Java 8 differ in day and timestamp formatting (space vs - for GB, and additional commas separating day and time for timestamp. **/
@AddLogger
class DateTimeFormatterTest {

    static char hy = ' '          // hyphen or space to separate date fields in GB ('-' for 1.8, space for 9 and 10
    static String comma = ",";    // separates time and date (empty for 1.8, comma for 9 and 10)

    def void print(String language, String country) {
        LOGGER.info("Date / time format for {}, {}", language, country)
        val now = LocalDateTime.now()
        val time = now.toLocalTime
        val date = now.toLocalDate
        val locale = new Locale(language, country)
//        val fmt = ISODateTimeFormat.basicDateTime
//        val string = fmt.print(now)
//        println('''It is now «string»''')

        val formats = #[ SHORT, MEDIUM ] //  LONG, FULL not working, see https://stackoverflow.com/questions/59531046/java-time-datetimeexception-unable-to-extract-zoneid-from-temporal
        formats.forEach[
            val nowS  = now.format(DateTimeFormatter.ofLocalizedDateTime(it, it).withLocale(locale));
            val dayS  = date.format(DateTimeFormatter.ofLocalizedDate(it).withLocale(locale));
            val timeS = time.format(DateTimeFormatter.ofLocalizedTime(it).withLocale(locale));
            LOGGER.info("For modifier {}, the date is {}, the time {}, and full {}", it, dayS, timeS, nowS)
        ]
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
                            Jahr:       ${t(d.ts, 'MM', 'i', 'yyyy')},
                            Wochentag:  ${t(d.ts, 'MM', 'i', 'EEEE MMMM')},
                        ''',
                    "en" ->
                        '''
                            Day:        ${t(d.day, 'M-')},
                            Time:       ${t(d.time, '-M')},
                            Timestamp:  ${t(d.ts, 'MM')}
                            Day:        ${t(d.ts, 'M-', 'D')},
                            Time:       ${t(d.ts, '-M', 'T')},
                            Custom:     ${t(d.ts, 'MM', 'i', 'yyyy')},
                            DoW, month: ${t(d.ts, 'MM', 'i', 'EEEE MMMM')},
                        '''
                }.get(selector.languageCode)
            ]
        }
    }

    @BeforeAll
    def static void setup() {
        val oldId = ZoneId.systemDefault().getId();
        if ("GMT" != oldId) {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            LOGGER.info("Setting time zone to GMT (UTC) (was {} before)", oldId);
        } else {
            LOGGER.info("Time zone already set to GMT - good");
        }
        Jdp.reset
        Jdp.bindInstanceTo(new MockedDocModuleCfgDtoResolver, IDocModuleCfgDtoResolver)
        Jdp.bindInstanceTo(new MockedPersistenceAccess, IDocPersistenceAccess)

        // set Java 8 / later differences
        val version = System.getProperty("java.version")
        LOGGER.info("Java version is {}", version)
        if (version.startsWith("1.8")) {
            comma = ""
            hy = "-"
        }
    }

    @BeforeEach
    def void clearCache() {
        // because we feed different data into the formatter with the same key, the cache must be invalidated before every test
        DocFormatter.clearCache
    }

    val data = #{
        "day"  -> LocalDate.of(2016, 8, 15),
        "time" -> LocalTime.of(17, 26, 58),
        "ts"   -> LocalDateTime.of(2016, 8, 15, 17, 26, 58)
    }

    @Test
    def void verySimpleTest() {
        val ts = LocalDateTime.of(2016, 8, 15, 17, 26, 58)
        LOGGER.info("ISO timestamp is {}", ts.format(DateTimeFormatter.ISO_DATE_TIME))
        val localFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
        .withLocale(Locale.GERMAN) //.withZone(ZoneId.of("Europe/Berlin"));
        LOGGER.info("local timestamp is {}", ts.format(localFormatter))
        val inst = ts.toInstant(ZoneOffset.UTC)
        val zdt = inst.atZone(ZoneId.of("Europe/Berlin"))
        LOGGER.info("local instant is {}", zdt.format(localFormatter))
    }

    @Test
    def void testDateTimeDocFormatterDE() {
        val actual = new DocFormatter().formatDocument("TEST", "TEST", TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
            languageCode = "de"
            countryCode  = "DE"
            currencyCode = "EUR"
        ], "Europe/Berlin", data, null)
        val expected = '''
            Tagesdatum: 15.08.2016,
            Uhrzeit:    17:26:58,
            Datum+Zeit: 15.08.2016«comma» 19:26:58
            Tagesdatum: 15.08.2016,
            Uhrzeit:    19:26:58,
            Jahr:       2016,
            Wochentag:  Montag August,
        '''
        LOGGER.info("Generated text is {}, current zone ID {}", actual.text, ZoneId.systemDefault.id)
        Assertions.assertEquals(expected, actual.text)
    }

    @Test
    def void testDateTimeDocFormatterUTC() {
        val actual = new DocFormatter().formatDocument("TEST", "TEST", TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
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
        LOGGER.info("Generated text is {}, current zone ID {}", actual.text, ZoneId.systemDefault.id)
        Assertions.assertEquals(expected, actual.text)
    }

    @Test
    def void testDateTimeDocFormatterGB() {
        val actual = new DocFormatter().formatDocument("TEST", "TEST", TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
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
        LOGGER.info("Generated text is {}", actual.text)
        Assertions.assertEquals(expected, actual.text)
    }

    @Test
    def void testDateTimeDocFormatterUS() {
        val actual = new DocFormatter().formatDocument("TEST", "TEST", TemplateType.DOCUMENT_ID, 'testId', new DocumentSelector => [
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
        val actualPostprocessed = actual.text.replace(DocConstants.NONBREAKING_SPACE, DocConstants.REGULAR_SPACE)
        LOGGER.info("Generated text is {}", actualPostprocessed)
        Assertions.assertEquals(expected, actualPostprocessed)
    }
}
