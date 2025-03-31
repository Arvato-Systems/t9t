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
package com.arvatosystems.t9t.io;

import java.time.ZoneId;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.CSVConfiguration;
import de.jpaw.bonaparte.core.CSVStyle;

public final class CSVTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVTools.class);

    private CSVTools() { }

    private static CSVStyle t2b(final CsvDateTimeStyleType t) {
        return t == null ? null : CSVStyle.factory(t.getToken());
    }

    public static CSVConfiguration getCsvConfiguration(final CsvConfigurationDTO cfgDTO) {
        return getCsvConfiguration(cfgDTO, true);
    }

    public static CSVConfiguration getCsvConfiguration(final CsvConfigurationDTO cfgDTO, final boolean useRegExp) {
        if (cfgDTO == null)
            return CSVConfiguration.CSV_DEFAULT_CONFIGURATION;
        else {
            // create a custom configuration
            final Integer quote = cfgDTO.getQuote();

            return new CSVConfiguration(
                cfgDTO.getSeparator(),
                quote == null ? null : Character.valueOf((char)quote.intValue()),
                optRegExp(useRegExp, cfgDTO.getQuoteReplacement()),
                optRegExp(useRegExp,  cfgDTO.getCtrlReplacement()),
                cfgDTO.getQuoteDates(),
                cfgDTO.getRemovePoint(),
                optRegExp(useRegExp, cfgDTO.getMapStart()),
                optRegExp(useRegExp, cfgDTO.getMapEnd()),
                optRegExp(useRegExp, cfgDTO.getArrayStart()),
                optRegExp(useRegExp, cfgDTO.getArrayEnd()),
                optRegExp(useRegExp, cfgDTO.getObjectStart()),
                optRegExp(useRegExp, cfgDTO.getObjectEnd()),
                optRegExp(useRegExp, cfgDTO.getBooleanTrue()),
                optRegExp(useRegExp, cfgDTO.getBooleanFalse()),
                cfgDTO.getLanguageCode() != null ? Locale.forLanguageTag(cfgDTO.getLanguageCode()) : null,
                cfgDTO.getTimeZone() != null ? ZoneId.of(cfgDTO.getTimeZone()) : null,
                t2b(cfgDTO.getDayStyle()),
                t2b(cfgDTO.getTimeStyle()),
                cfgDTO.getCustomDayFormat(),
                cfgDTO.getCustomTimeFormat(),
                cfgDTO.getCustomTimeWithMsFormat(),
                cfgDTO.getCustomTsFormat(),
                cfgDTO.getCustomTsWithMsFormat(),
                cfgDTO.getZeroPadNumbers(),
                cfgDTO.getRightPadNumbers(),
                cfgDTO.getUseGrouping()
            );
        }
    }

    private static final char[] CHAR_MAP = new char[128];
    static {
        for (int i = 0; i < 128; ++i) {
            CHAR_MAP[i] = (char)i;
        }
        CHAR_MAP['b'] = '\b';
        CHAR_MAP['f'] = '\f';
        CHAR_MAP['r'] = '\r';
        CHAR_MAP['n'] = '\n';
        CHAR_MAP['t'] = '\t';
        CHAR_MAP['x'] = '\0';
        CHAR_MAP['0'] = '\0';
        CHAR_MAP['1'] = '\0';
        CHAR_MAP['2'] = '\0';
        CHAR_MAP['3'] = '\0';
    }

    public static String optRegExp(final boolean doRegExp, final String in) {
        if (!doRegExp || in == null || in.isEmpty()) {
            return in;
        }
        final int len = in.length();
        final StringBuilder out = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            final char c = in.charAt(i);
            if (c != '\\' || i == len - 1) {
                out.append(c);
            } else {
                final int x = in.charAt(++i);  // eats the backslash
                if (x < 0 || x >= 128) {
                    out.append((char)x);
                } else {
                    final char cvt = CHAR_MAP[x];
                    if (cvt != '\0') {
                        out.append(cvt);
                    } else {
                        try {
                            if (x == 'x') {
                                // check for 4 character hex code string
                                if (i < len - 4) {
                                    final String hexCode = in.substring(i + 1, i + 5);  // x is NOT part of the sequence
                                    i += 4;
                                    out.append((char)(Integer.valueOf(hexCode, 16).intValue()));
                                } else {
                                    LOGGER.warn("premature end of \\x escape sequence (expected 4 more hex digits)");
                                    // we just ignore the backslash in this case
                                    out.append('x'); // use x verbatim
                                }
                            } else {
                                // check for 3 digit octal notation or hex string
                                if (i < len - 2) {
                                    final String hexCode = in.substring(i, i + 3);  // first digit is part of the sequence
                                    i += 2;
                                    out.append((char)(Integer.valueOf(hexCode, 8).intValue()));
                                } else {
                                    LOGGER.warn("premature end of octal escape sequence (expected 3 more octal digits)");
                                    // we just ignore the backslash in this case
                                    out.append((char)x); // use digit verbatim
                                }
                            }
                        } catch (final Exception e) {
                            LOGGER.error("Number conversion exception: ", e);
                        }
                    }
                }
            }
        }
        return out.toString();
    }
}
