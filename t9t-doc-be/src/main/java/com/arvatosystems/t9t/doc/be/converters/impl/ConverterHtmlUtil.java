/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.doc.be.converters.impl;

import com.google.common.html.HtmlEscapers;

import java.util.Map;

public final class ConverterHtmlUtil {
    private static final String EMPTY_STRING = "";
    private ConverterHtmlUtil() { }

    public static String getDimension(final Map<String, Object> z, final String key) {
        final Object o = z.get(key);
        if (o instanceof Number) {
            return o.toString() + "px";
        }
        if (o != null) {
            return o.toString();
        }
        return EMPTY_STRING;
    }

    public static String getSizeSpec(final Map<String, Object> z) {
        if (z != null) {
            final String widthStr = getDimension(z, "width");
            final String heightStr = getDimension(z, "height");
            if (widthStr != null && heightStr != null && !EMPTY_STRING.equals(widthStr) && !EMPTY_STRING.equals(heightStr)) {
                return " width=\"" + widthStr + "\" height=\"" + heightStr + "\"";
            }
        }
        return EMPTY_STRING;
    }

    public static String addSpec(final Map<String, Object> z, final String keyword) {
        if (z == null) {
            return EMPTY_STRING;
        }
        final Object value = z.get(keyword);
        if (value != null) {
            return " " + keyword + "=\"" + HtmlEscapers.htmlEscaper().escape(value.toString()) + "\"";
        }
        return EMPTY_STRING;
    }

    public static String addBoolean(final Map<String, Object> z, final String keyword) {
        if (z == null) {
            return EMPTY_STRING;
        }
        final Object value = z.get(keyword);
        if (value instanceof Boolean boolValue) {
            if (boolValue) {
                return " " + keyword;
            }
        }
        return EMPTY_STRING;
    }
}
