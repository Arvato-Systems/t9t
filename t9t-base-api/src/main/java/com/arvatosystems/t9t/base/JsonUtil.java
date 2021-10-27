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
package com.arvatosystems.t9t.base;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.json.JsonException;

public final class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    private JsonUtil() {
    }

    private static int hex(final char c) {
        if (c >= '0' && c <= '9')
            return c - '0';
        if (c >= 'A' && c <= 'F')
            return c - 'A' + 10;
        if (c >= 'a' && c <= 'f')
            return c - 'a' + 10;
        throw new JsonException(JsonException.JSON_BAD_ESCAPE, "invalid hex digit " + c);
    }

    public static String unescapeJson(final String s) {
        if (s == null)
            return null;
        boolean escaped = false;
        final int l = s.length();
        final StringBuilder t = new StringBuilder(l);
        int i = 0;
        while (i < l) {
            char c = s.charAt(i);
            if (c == '\\') {
                // unescape
                escaped = true;
                if (i == l - 1)
                    throw new JsonException(JsonException.JSON_BAD_ESCAPE, "Escape symbol as last char");
                c = s.charAt(++i);
                switch (c) {
                case 'b':
                    c = '\b';
                    break;
                case 'r':
                    c = '\r';
                    break;
                case 'n':
                    c = '\n';
                    break;
                case 't':
                    c = '\t';
                    break;
                case 'f':
                    c = '\f';
                    break;
                case 'u':
                    if (i >= l - 4)
                        throw new JsonException(JsonException.JSON_BAD_ESCAPE, "Escape symbol u too close to end of string");
                    int cc = hex(s.charAt(++i)) << 24;
                    cc |= hex(s.charAt(++i)) << 16;
                    cc |= hex(s.charAt(++i)) << 8;
                    cc |= hex(s.charAt(++i));
                    c = (char)cc;
                    break;
                default:
                    // c = c (1:1, as for \\, \" etc.
                    break;
                }
            }
            t.append(c);
            ++i;
        }

        return escaped ? t.toString() : s;
    }

    /** Safe getter for a z field value, also works if z itself is null. */
    public static Object getZEntry(final Map<String, Object> z, final String key) {
        return z == null ? null : z.get(key);
    }

    /** Safe getter for a z field value, also works if z itself is null, returns a String typed result, if required, by conversion. */
    public static String getZString(final Map<String, Object> z, final String key, final String defaultValue) {
        final Object value = getZEntry(z, key);
        return value == null ? defaultValue : value.toString();
    }

    /** Safe getter for a z field value, also works if z itself is null, returns a Long typed result, if required, by conversion. */
    public static Long getZLong(final Map<String, Object> z, final String key, final Long defaultValue) {
        final Object value = getZEntry(z, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return (Long)value;
        }
        if (value instanceof Number) {
            return ((Number)value).longValue();
        }
        try {
            // attempt parsing a number
            return Long.parseLong(value.toString());
        } catch (final Exception e) {
            LOGGER.error("Cannot convert value {} for {} to numeric: {}", value, key, e);
        }
        LOGGER.error("Required Number for z entry {}, but got {}", key, value.getClass().getCanonicalName());
        throw new T9tException(T9tException.INVALID_REQUEST_PARAMETER_TYPE, "Required a Number for z entry " + key
                + ", but got " + value.getClass().getCanonicalName());
    }

    /** Safe getter for a z field value, also works if z itself is null, returns an Integer typed result, if required, by conversion. */
    public static Integer getZInteger(final Map<String, Object> z, final String key, final Integer defaultValue) {
        final Object value = getZEntry(z, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        try {
            // attempt parsing a number
            return Integer.parseInt(value.toString());
        } catch (final Exception e) {
            LOGGER.error("Cannot convert value {} for {} to numeric: {}", value, key, e);
        }
        LOGGER.error("Required Number for z entry {}, but got {}", key, value.getClass().getCanonicalName());
        throw new T9tException(T9tException.INVALID_REQUEST_PARAMETER_TYPE, "Required a Number for z entry " + key
                + ", but got " + value.getClass().getCanonicalName());
    }
}
