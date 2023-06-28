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
import java.util.Locale;

import org.zkoss.util.resource.Labels;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;

public abstract class AbstractDecimalFormatConverter<T> implements IItemConverter<T> {

    protected final String format;

    private AbstractDecimalFormatConverter(final String format) {
        if (format == null) {
            throw new NullPointerException("format not set in constructor");
        }
        this.format = Labels.getLabel(format, this.getPattern());
    }

    public AbstractDecimalFormatConverter() {
        this("com.decimal.format");
    }

    @Override
    public boolean isRightAligned() {
        return true;
    }

    protected abstract String getPattern();

    protected DecimalFormat getLocalizedDecimalFormat(final String pattern, final int minimumFractionDigits) {
        final DecimalFormat df = getLocalizedDecimalFormat(pattern);
        df.setMinimumFractionDigits(minimumFractionDigits);
        return df;
    }

    protected DecimalFormat getLocalizedDecimalFormat(final String pattern) {
        final Locale userLocale = ApplicationSession.get().getUserLocale();
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(userLocale);
        df.applyPattern(pattern);
        return df;
    }
}
