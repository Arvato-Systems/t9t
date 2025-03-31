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
package com.arvatosystems.t9t.zkui.util;

import java.util.Currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.util.CharTestsASCII;

public final class CurrencyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyUtil.class);

    private CurrencyUtil() { }

    /** Determines the number of fractional digits for a given currency (or immediate number). */
    public static Integer getFractionalDigits(String currency, String fieldName) {
        if (currency == null || currency.isBlank()) {
            return null;
        }
        if (currency.length() == 1 && Character.isDigit(currency.charAt(0))) {
            return Integer.valueOf(currency);
        } else if (currency.length() == 3 && CharTestsASCII.isUpperCase(currency)) {
            // assume it is a currency
            try {
                Currency curr = Currency.getInstance(currency);
                return curr.getDefaultFractionDigits();
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Cannot get currency {} for {}", currency, fieldName);
            }
        }
        return null;
    }
}
