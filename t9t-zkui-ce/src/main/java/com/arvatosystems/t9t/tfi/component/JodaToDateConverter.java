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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;

public class JodaToDateConverter implements Converter {
    private static final Logger LOGGER               = LoggerFactory.getLogger(JodaToDateConverter.class);

    /** default pattern: MM/DD/YYYY */
    private static final String   DEFAULT_DATA_PATTERN = "MM/DD/YYYY";

    private SimpleDateFormat      dateFormat           = null;

    /**
     * You can use a i18n string like 'com.datetime.format' or the concrete pattern like 'MM/dd/yyyy HH:mm:ss'
     * @param defaultDatePattern label or pattern
     */
    public JodaToDateConverter(String defaultDatePattern) {
        String convertedDatePattern = null;

        try {
            if (defaultDatePattern == null) {
                String pattern = ZulUtils.translate("com", "dateFormat");
                if (pattern == null) {
                    pattern = DEFAULT_DATA_PATTERN;
                }
                convertedDatePattern = pattern;
            } else {
                convertedDatePattern = Labels.getLabel(defaultDatePattern);
                if (convertedDatePattern == null) {
                    convertedDatePattern = defaultDatePattern;
                }
            }

            dateFormat = new SimpleDateFormat(convertedDatePattern);
        } catch (Exception e) {
            LOGGER.error("The given date pattern >{}< (converted:>{}<) is not valid", defaultDatePattern, convertedDatePattern);
        }
    }

    /**
     * Create converter with default pattern {@link JodaToDateConverter#DEFAULT_DATA_PATTERN}
     */
    public JodaToDateConverter() {
        this(null);
    }

    /**
     * it will return formatted string. defined by {@link #JodaToDateConverter()} or {@link #JodaToDateConverter(String)}
     */
    @Override
    public String getFormattedLabel(Object value, Object wholeDataObject, String fieldName) {
        if (value == null) {
            return null;
        } // do nothing
        if (dateFormat == null) {
            return String.valueOf(value);
        }

        if (value instanceof LocalDate) {
            return ApplicationSession.get().format((LocalDate)value);
        } else if (value instanceof Date) {
            return dateFormat.format((value));
        }else if (value instanceof LocalDateTime) {
            return ApplicationSession.get().format((LocalDateTime)value);
        }else if (value instanceof Instant) {
            return ApplicationSession.get().format((Instant)value);
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported");
        }
    }

    /**
     * it will return a java.util.Date object
     */
    @Override
    public Object getConvertedValue(Object value, Object wholeDataObject, String fieldName) {
        if (value == null) {
            return null; // do nothing
        }

        if (value instanceof LocalDate) {
            return ApplicationSession.get().toDate((LocalDate)value);
        } else if (value instanceof LocalDateTime) {
            return ApplicationSession.get().toDate((LocalDateTime)value);
        } else if (value instanceof Date) {
            return value;
        } else if (value instanceof Instant) {
            return ApplicationSession.get().toDate((Instant)value);
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported");
        }
    }
}
