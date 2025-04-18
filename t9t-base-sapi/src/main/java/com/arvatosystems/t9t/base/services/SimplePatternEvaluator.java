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
package com.arvatosystems.t9t.base.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;

import de.jpaw.util.CharTestsASCII;

public final class SimplePatternEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePatternEvaluator.class);

    private static final String FORMATTED_PATTERN_FORMAT = "\\$\\{%s(\\|([^\\}\\{]+))?\\}";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    private SimplePatternEvaluator() {
        // prevent instantiation of util class
    }

    private static boolean isForbidden(final String s, final int i, final String forbiddenChars, final boolean checkForFilename) {
        final char c = s.charAt(i);
        if (checkForFilename && c == '.') {
            return i == 0 || i >= s.length() - 1 || !CharTestsASCII.isAsciiAlnum(s.charAt(i + 1));
        } else {
            return c < 0x20 || c >= 0x7f || forbiddenChars.indexOf(c) >= 0;
        }
    }

    /** Replaces characters which should not be inserted into a string because they might alter the folder by an upper case X. */
    private static String sanitizer(final String input, final String forbiddenChars, final boolean isFilenameCheck) {
        // perform a check first, to avoid creating new objects
        boolean needChange = false;
        for (int i = 0; i < input.length(); ++i) {
            if (isForbidden(input, i, forbiddenChars, isFilenameCheck)) {
                needChange = true;
                break;
            }
        }
        if (!needChange) {
            return input;
        }
        final StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ++i) {
            sb.append(isForbidden(input, i, forbiddenChars, isFilenameCheck) ? 'X' : input.charAt(i));
        }
        return sb.toString();
    }

    /** Replaces any variable references in pattern by lookup from the replacements map, using a specific sanitizer suitable for file names. */
    public static String evaluate(final String pattern, final Map<String, Object> patternReplacements) {
        return evaluate(pattern, patternReplacements, s -> sanitizer(s, ":/\\. (){}[]$^~?&%=", true), "NULL");
    }

    /** Replaces any variable references in pattern by lookup from the replacements map, using a generic sanitizer. */
    public static String evaluate(final String pattern, final Map<String, Object> patternReplacements, final Function<String, String> sanitizer,
      final String valueForNullOrEmpty) {
        String result = pattern;
        for (final Map.Entry<String, Object> replacementEntry : patternReplacements.entrySet()) {
            final Object replacementValue = replacementEntry.getValue();
            if (replacementValue instanceof Temporal) {
                final Pattern formattedPattern = Pattern.compile(String.format(FORMATTED_PATTERN_FORMAT, replacementEntry.getKey()));
                final Matcher matcher = formattedPattern.matcher(result);

                // we need to match ALL matching patterns which each can have different date formats
                // e.g. ${now|YYYY} and ${now|MM}
                while (matcher.find()) {
                    String dateFormat = DEFAULT_DATE_FORMAT;
                    final String toBeReplaced = matcher.group(0);
                    final String passedDateFormat = matcher.group(2);

                    if (passedDateFormat != null && !passedDateFormat.isEmpty()) {
                        dateFormat = passedDateFormat;
                    }

                    DateTimeFormatter formatter;
                    try {
                        formatter = DateTimeFormatter.ofPattern(dateFormat);
                    } catch (final IllegalArgumentException ex) {
                        LOGGER.warn("Invalid date/time format \"{}\" for pattern \"{}\". Using default format.", dateFormat, replacementEntry.getKey());
                        formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
                    }

                    final String formattedDate;
                    if (replacementValue instanceof LocalDate rld) {
                        formattedDate = rld.format(formatter);
                    } else if (replacementValue instanceof LocalDateTime rldt) {
                        formattedDate = rldt.format(formatter);
                    } else if (replacementValue instanceof Instant ri) {
                        formattedDate = LocalDateTime.ofInstant(ri, ZoneOffset.UTC).format(formatter);
                    } else {
                        LOGGER.error("Class {} not (yet) supported for date/time formats.", replacementValue.getClass().getCanonicalName());
                        formattedDate = "***NYS***";
                    }

                    result = result.replaceAll(Pattern.quote(toBeReplaced), formattedDate);
                }
            } else {
                final String key = Pattern.quote("${" + replacementEntry.getKey() + "}");
                final Object tmp = replacementEntry.getValue();
                final String value;
                if (tmp == null) {
                    value = valueForNullOrEmpty;
                } else {
                    final String tmp2 = tmp.toString();
                    value = (tmp2 == null || tmp2.isEmpty()) ? valueForNullOrEmpty : sanitizer.apply(tmp2);
                }
                result = result.replaceAll(key, T9tUtil.nvl(value, ""));
            }
        }
        return result;
    }
}
