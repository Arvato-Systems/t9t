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
package com.arvatosystems.t9t.zkui.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

import com.arvatosystems.t9t.zkui.util.CurrencyUtil;

import de.jpaw.fixedpoint.FixedPointBase;

/**
 * This is a ZK Data binding converter that is only a formatter (for labels etc) to provide read only numbers.
 */
public class FixedPointToStringConverter implements Converter<String, FixedPointBase<?>, Component> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedPointToStringConverter.class);

    @Override
    public String coerceToUi(FixedPointBase<?> fp, Component component, BindContext ctx) {
        if (fp == null) {
            return null;
        }
        final Object decimals = ctx.getConverterArg("decimals");
        if (decimals == null) {
            return fp.toString();
        }
        if (decimals instanceof Number number) {
            final int digits = number.intValue();
            if (digits >= 0 && digits <= 18) {
                return fp.toString(digits);
            }
            LOGGER.warn("{} is not a valid number of decimal digits", digits);
        } else if (decimals instanceof String strDecimals) {
            // interpret as currency and obtain the decimals from that
            final Integer digits = CurrencyUtil.getFractionalDigits(strDecimals, "FixedPointToStringConverter");
            if (digits != null) {
                return fp.toString(digits);
            }
        }
        // fall through and still print some data
        return fp.toString();
    }

    @Override
    public FixedPointBase<?> coerceToBean(String value, Component component, BindContext ctx) {
        throw new UnsupportedOperationException("FixedPointToStringConverter is a one-way converter");
    }
}
