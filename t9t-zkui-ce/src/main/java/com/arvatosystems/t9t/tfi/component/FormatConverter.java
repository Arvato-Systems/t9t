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
package com.arvatosystems.t9t.tfi.component;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;

public class FormatConverter implements Converter {

    private String format;

    public FormatConverter(String format) {
        if (format == null) throw new NullPointerException("format not set in constructor");
        this.format = Labels.getLabel(format);
        if (this.format == null) this.format = format;
    }

    @Override
    public String getFormattedLabel(Object value, Object wholeDataObject, String fieldName) {
        if (value == null) return null;
        if (value instanceof String) return null; // when ZK not able to get the field name in object. It will return a string of error.

        if (value instanceof Number) {
            return getLocalizedDecimalFormat(this.format).format((Number) value);
        } else if (value instanceof Date) {
            return getDateFormat(this.format).format((Date) value);
        } else if (value instanceof LocalDateTime) {
            return getDateFormat(this.format).format(((LocalDateTime) value).toDate());
        } else if (value instanceof LocalTime) {
            return getDateFormat(this.format).format(((LocalTime) value).toDateTimeToday().toDate());
        } else if (value instanceof LocalDate) {
            return getDateFormat(this.format).format(((LocalDate) value).toDate());
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported. Field:" + fieldName + "->" + value);
        }
    }

    @Override
    public Object getConvertedValue(Object value, Object wholeDataObject, String fieldName) {
        return value;
    }

    private static DecimalFormat getLocalizedDecimalFormat(String pattern) {
        final DecimalFormat df =
                (DecimalFormat) NumberFormat.getInstance(Locales.getCurrent());
        df.applyPattern(pattern);
        return df;
    }

    private static SimpleDateFormat getDateFormat(String pattern) {
        final SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df;
    }

}
