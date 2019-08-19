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
package com.arvatosystems.t9t.rep.be.util;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.base.BaseLocal;
import org.joda.time.format.DateTimeFormat;

public class TimeFormatter {
    public static String formatTimetoStr(LocalDateTime dateTime) {

        return dateTime.toString("yyyy-MM-dd HH:mm:SS");

    }

    /**
     * Format the UTC time zone date and format to string based on the locale and time zone
     * @param dateTime must be in UTC
     * @param timeZone
     * @param locale
     * @return formatted UTC time based on locale and time zone
     */
    public static String formatDateTime(BaseLocal dateTime, String timeZone, Locale locale) {
        if (dateTime == null) {
            return null;
        }
        String formattedDateTime = null;
        if (dateTime instanceof LocalDate) {
            LocalTime clientLocalTime = new LocalTime(dateTime, DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone)));
            formattedDateTime = DateTimeFormat.mediumDate().withLocale(locale).print(clientLocalTime);

        } else if (dateTime instanceof LocalDateTime) {
            LocalDateTime clientLocalDateTime = new LocalDateTime(dateTime, DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone)));
            formattedDateTime = DateTimeFormat.mediumDateTime().withLocale(locale).print(clientLocalDateTime);
        }
        return formattedDateTime;
    }

    /**
     * Format and add the time zone different pass in to the method and return the formatted time zone based on the time zone and locale
     * @param dateTime must be in UTC
     * @param timeZone
     * @param locale
     * @return formatted date/datetime (plus the pass in time zone difference) based on the time zone and locale
     */
    public static String formatDateTimeWithTargetTimeZone(BaseLocal dateTime, String timeZone, Locale locale) {
        if (dateTime == null) {
            return null;
        }
        String formattedDateTime = null;
        if (dateTime instanceof LocalDate) {
            LocalTime clientLocalTime = new LocalTime(dateTime, DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone)));
            formattedDateTime = DateTimeFormat.mediumDate().withLocale(locale).print(clientLocalTime);

        } else if (dateTime instanceof LocalDateTime) {
            LocalDateTime clientLocalDateTime = ((LocalDateTime) dateTime)
                    .plusMillis(DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone)).getOffset(((LocalDateTime) dateTime).toDateTime()));
            formattedDateTime = DateTimeFormat.mediumDateTime().withLocale(locale).print(clientLocalDateTime);
        }
        return formattedDateTime;
    }

    /** Format the UTC time zone date and format to string based on the locale and time zone
     * @param dateTimeInstant
     * @param timeZone
     * @param locale
     * @return formatted date/date time (plus the pass in time zoen difference) based on the time zone and locale
     */
    public static String formatDateTime(Instant dateTimeInstant, String timeZone, Locale locale) {
        if (dateTimeInstant == null) {
            return null;
        }
        LocalDateTime localDateTime = new LocalDateTime(dateTimeInstant);
        return formatDateTime(localDateTime, timeZone, locale);
    }

    /** Format and add the time zone different pass in to the method and return the formatted time zone based on the time zone and locale
     * @param dateTimeInstant
     * @param timeZone
     * @param locale
     * @return  formatted date/datetime (plus the pass in time zone difference) based on the time zone and locale
     */
    public static String formatDateTimeWithTargetTimeZone(Instant dateTimeInstant, String timeZone, Locale locale) {
        if (dateTimeInstant == null) {
            return null;
        }
        LocalDateTime localDateTime = new LocalDateTime(dateTimeInstant);
        return formatDateTimeWithTargetTimeZone(localDateTime, timeZone, locale);
    }
}
