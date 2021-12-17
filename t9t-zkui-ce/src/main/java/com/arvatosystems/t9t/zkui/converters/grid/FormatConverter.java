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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;

public class FormatConverter implements IItemConverter {

    private String format;

    public FormatConverter(String format) {
        if (format == null) throw new NullPointerException("format not set in constructor");
        this.format = Labels.getLabel(format);
        if (this.format == null) this.format = format;
    }

    @Override
    public String getFormattedLabel(Object value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        if (value == null) return null;
        if (value instanceof String) return null; // when ZK not able to get the field name in object. It will return a string of error.

        if (value instanceof Number) {
            return getLocalizedDecimalFormat(this.format).format((Number) value);
        } else if (value instanceof Date) {
            return getDateFormat(this.format).format((Date) value);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(this.format));
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).format(DateTimeFormatter.ofPattern(this.format));
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ofPattern(this.format));
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported. Field:" + fieldName + "->" + value);
        }
    }

    private static DecimalFormat getLocalizedDecimalFormat(String pattern) {
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locales.getCurrent());
        df.applyPattern(pattern);
        return df;
    }

    private static SimpleDateFormat getDateFormat(String pattern) {
        final SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df;
    }
}
