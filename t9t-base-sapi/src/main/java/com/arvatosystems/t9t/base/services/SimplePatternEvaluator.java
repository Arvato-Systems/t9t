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
package com.arvatosystems.t9t.base.services;

import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimplePatternEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePatternEvaluator.class);

    private static final String PATTERN_FORMAT = "${%s}";
    private static final String FORMATTED_PATTERN_FORMAT = "\\$\\{%s(\\|([^\\}\\{]+))?\\}";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    private SimplePatternEvaluator() {
        // prevent instantiation of util class
    }

    private static boolean isDateReplacement(final Object obj) {
        return (obj instanceof Date || obj instanceof LocalDateTime || obj instanceof LocalDate || obj instanceof DateTime || obj instanceof Instant);
    }

    private static ReadablePartial convertToJodaCompatibleDateTime(final Object obj) {
        if (obj instanceof ReadablePartial) {
            return (ReadablePartial) obj;
        } else if (obj instanceof Date) {
            return LocalDateTime.fromDateFields((Date) obj);
        } else {
            throw new UnsupportedOperationException("Unsupported Object type to be converted");
        }
    }

    public static String evaluate(final String pattern, final Map<String, Object> patternReplacements) {
        String result = pattern;
        for (Map.Entry<String, Object> replacementEntry : patternReplacements.entrySet()) {
            Object replacementValue = replacementEntry.getValue();
            if (isDateReplacement(replacementValue)) {
                Pattern formattedPattern = Pattern.compile(String.format(FORMATTED_PATTERN_FORMAT, replacementEntry.getKey()));
                Matcher matcher = formattedPattern.matcher(result);

                // we need to match ALL matching patterns which each can have different date formats
                // e.g. ${now|YYYY} and ${now|MM}
                while (matcher.find()) {
                    String dateFormat = DEFAULT_DATE_FORMAT;
                    String toBeReplaced = matcher.group(0);
                    String passedDateFormat = matcher.group(2);

                    if (passedDateFormat != null && !passedDateFormat.isEmpty()) {
                        dateFormat = passedDateFormat;
                    }

                    DateTimeFormatter formatter;
                    try {
                        formatter = DateTimeFormat.forPattern(dateFormat);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.warn("Invalid date/time format \"{}\" for pattern \"{}\". Using default format.", dateFormat, replacementEntry.getKey());
                        formatter = DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT);
                    }

                    final String formattedDate;
                    if (replacementValue instanceof ReadableInstant) {
                        formattedDate = formatter.print((ReadableInstant) replacementValue);
                    } else {
                        formattedDate = formatter.print(convertToJodaCompatibleDateTime(replacementValue));
                    }

                    result = result.replaceAll(Pattern.quote(toBeReplaced), formattedDate);
                  }
            } else {
                String key = Pattern.quote(String.format(PATTERN_FORMAT, replacementEntry.getKey()));
                String value = replacementEntry.getValue() == null ? null : replacementEntry.getValue().toString();
                result = result.replaceAll(key, value);
            }
        }
        return result;
    }
}
