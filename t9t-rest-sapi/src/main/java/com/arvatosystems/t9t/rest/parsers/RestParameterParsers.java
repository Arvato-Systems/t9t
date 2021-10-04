/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.rest.parsers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.core.MessageParserException;

/**
 * A collection of parsers.
 * These are used when the parameters are declared as string, and parsed manually.
 * The parsers throw an ApplicationException, because that is caught by the specific exception handler.
 */
public class RestParameterParsers {
    public static UUID parseUUID(String asString, String where, boolean required) {
        if (asString == null || asString.length() == 0) {
            if (required) {
                throw new T9tException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, where);
            }
        }
        try {
            return UUID.fromString(asString);
        } catch (Exception e) {
            throw new T9tException(MessageParserException.BAD_UUID_FORMAT, where);
        }
    }

    public static LocalDate parseLocalDate(String asString, String where, boolean required) {
        if (asString == null || asString.length() == 0) {
            if (required) {
                throw new T9tException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, where);
            }
        }
        try {
            return LocalDate.parse(asString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new T9tException(MessageParserException.FIELD_PARSE, where);
        }
    }

    public static LocalTime parseLocalTime(String asString, String where, boolean required) {
        if (asString == null || asString.length() == 0) {
            if (required) {
                throw new T9tException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, where);
            }
        }
        try {
            return LocalTime.parse(asString, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (Exception e) {
            throw new T9tException(MessageParserException.FIELD_PARSE, where);
        }
    }

    public static LocalDateTime parseLocalDateTime(String asString, String where, boolean required) {
        if (asString == null || asString.length() == 0) {
            if (required) {
                throw new T9tException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, where);
            }
        }
        try {
            return LocalDateTime.parse(asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            throw new T9tException(MessageParserException.FIELD_PARSE, where);
        }
    }

    public static Instant parseInstant(String asString, String where, boolean required) {
        if (asString == null || asString.length() == 0) {
            if (required) {
                throw new T9tException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, where);
            }
        }
        try {
            return LocalDateTime.parse(asString, DateTimeFormatter.ISO_INSTANT).toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            throw new T9tException(MessageParserException.FIELD_PARSE, where);
        }
    }
}
