/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.json.JsonException;
import de.jpaw.util.ConfigurationReaderFactory;

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

    /** Safe getter for a z field value, also works if z itself is null, returns an Instant typed result, if required, by conversion. */
    public static Instant getZInstant(final Map<String, Object> z, final String key, final Instant defaultValue) {
        final Object value = getZEntry(z, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Instant) {
            return (Instant)value;
        }
        // in JSON, an instant could have been persisted as a numeric value, in seconds since the epoch
        if (value instanceof Number) {
            return Instant.ofEpochSecond(((Number)value).longValue());
        }
        // last resort: parse from string
        return Instant.parse(value.toString());
    }

    public static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.json", null);
    public static final boolean UNWRAP_JSON = Boolean.TRUE.equals(CONFIG_READER.getBooleanProperty("t9t.json.unwrapkvp"));

    /** Unwraps the XML-workaround structure into plain JSON, if configured. */
    public static Map<String, Object> unwrapKvpIfWanted(final Map<String, Object> z) {
        return UNWRAP_JSON ? unwrapKvp(z) : z;
    }

    /** Unwraps the XML-workaround structure into plain JSON. */
    public static Map<String, Object> unwrapKvp(final Map<String, Object> z) {
        if (z == null || z.isEmpty()) {
            return z;
        }
        final Object kvp = z.get("kvp");
        if (kvp == null) {
            // no kvp entry: return structure as it is
            return z;
        } else {
            if (kvp instanceof List) {
                final List<Object> kvpList = (List<Object>) kvp;

                // convert kvp entries into regular z
                final Map<String, Object> result = new HashMap<>();
                // check for hybrid
                if (z.size() > 1) {
                    // more entries than just kvp
                    result.putAll(z);
                    result.remove("kvp");
                }
                // now transfer the list's entries
                for (Object entry : kvpList) {
                    if (entry instanceof Map) {
                        final Map<String, Object> entryMap = (Map<String, Object>) entry;
                        if (entryMap != null) {
                            final String key = (String) entryMap.get("key");
                            if (key == null) {
                                LOGGER.error("kvp list entry has no key");
                                throw new T9tException(T9tException.INVALID_WRAPPED_JSON, "kvp list entry has no key");
                            }
                            final Object oldVal = result.putIfAbsent(key, getValue(key, entryMap));
                            if (oldVal != null) {
                                LOGGER.info("key {} defined at least twice", key);
                                throw new T9tException(T9tException.INVALID_WRAPPED_JSON, "key " + key + " defined at least twice");
                            }
                        }
                    } else {
                        final String what = entry == null ? "NULL" : entry.getClass().getCanonicalName();
                        LOGGER.error("kvp list entry is not a Map but {}", what);
                        throw new T9tException(T9tException.INVALID_WRAPPED_JSON, "kvp list entry not a Map but " + what);
                    }
                }
                return result;
            } else {
                LOGGER.error("kvp entry found, but is not a List: {}", kvp.getClass().getCanonicalName());
                throw new T9tException(T9tException.INVALID_WRAPPED_JSON, "kvp entry not a List but " + kvp.getClass().getCanonicalName());
            }
        }
    }

    /** Extracts a single kvp entry from a wrapped entry. Type "obj" is not supported. */
    private static Object getValue(final String key, final Map<String, Object> entryMap) {
        // first, check scalar values
        final Object value = entryMap.get("value");
        if (value != null) {
            return value;
        }
        final Object bool = entryMap.get("bool");
        if (bool != null) {
            return bool;
        }
        final Object num = entryMap.get("num");
        if (num != null) {
            return num;
        }
        final Object values = entryMap.get("values");
        if (values instanceof List && !((List) values).isEmpty()) {
            return values;
        }
        final Object bools = entryMap.get("bools");
        if (bools instanceof List && !((List) bools).isEmpty()) {
            return bools;
        }
        final Object nums = entryMap.get("nums");
        if (nums instanceof List && !((List) nums).isEmpty()) {
            return nums;
        }
        LOGGER.error("kvp list entry for {} has no known value entry", key);
        throw new T9tException(T9tException.INVALID_WRAPPED_JSON, "kvp list entry for " + key + " has no known value entry");
    }
}
