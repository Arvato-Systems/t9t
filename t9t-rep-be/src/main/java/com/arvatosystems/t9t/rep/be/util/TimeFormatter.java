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
package com.arvatosystems.t9t.rep.be.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.util.Locale;

public final class TimeFormatter {

    private TimeFormatter() { }

    public static String formatTimetoStr(final LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Format the UTC time zone date and format to string based on the locale and time zone
     * @param dateTime must be in UTC
     * @param timeZone
     * @param locale
     * @return formatted UTC time based on locale and time zone
     */
    public static String formatDateTime(final Temporal dateTime, final String timeZone, final Locale locale) {
        if (dateTime == null) {
            return null;
        }
        String formattedDateTime = null;
        if (dateTime instanceof LocalDate d) {
            formattedDateTime = d.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale));
        } else if (dateTime instanceof Instant i) {
            final LocalDateTime d = LocalDateTime.ofInstant(i, ZoneId.of(timeZone));
            formattedDateTime = d.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(locale));
        } else if (dateTime instanceof LocalDateTime d) {
            if (timeZone != null && !timeZone.equals("UTC")) {
                d = d.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();  // timezone conversion
            }
            formattedDateTime = d.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(locale));
        }
        return formattedDateTime;
    }

    /** Format the UTC time zone date and format to string based on the locale and time zone
     * @param dateTimeInstant
     * @param timeZone
     * @param locale
     * @return formatted date/date time (plus the pass in time zone difference) based on the time zone and locale
     */
    public static String formatDateTime(final Instant dateTimeInstant, final String timeZone, final Locale locale) {
        if (dateTimeInstant == null) {
            return null;
        }
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(dateTimeInstant, ZoneOffset.UTC);
        return formatDateTime(localDateTime, timeZone, locale);
    }

    /** Format and add the time zone different pass in to the method and return the formatted time zone based on the time zone and locale
     * @param dateTimeInstant
     * @param timeZone
     * @param locale
     * @return  formatted date/datetime (plus the pass in time zone difference) based on the time zone and locale
     */
    public static String formatDateTimeWithTargetTimeZone(final Instant dateTimeInstant, final String timeZone, final Locale locale) {
        if (dateTimeInstant == null) {
            return null;
        }
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(dateTimeInstant, ZoneId.of(timeZone));
        return formatDateTime(localDateTime, timeZone, locale);
    }

    /** Format and add the time zone different pass in to the method and return the formatted time zone based on the time zone and locale
     * @param dateTimeInstant
     * @param timeZone
     * @param locale
     * @return  formatted date/datetime (plus the pass in time zone difference) based on the time zone and locale
     */
    public static String formatDateTimeWithTargetTimeZone(final LocalDateTime localDateTime, final String timeZone, final Locale locale) {
        if (localDateTime == null) {
            return null;
        }
        return formatDateTime(localDateTime, timeZone, locale);
    }
}
