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
package com.arvatosystems.t9t.rep.be.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class NumberFormatterCurrency {
    private static final Logger LOGGER = LoggerFactory.getLogger(NumberFormatterCurrency.class);
    private static final String NBSP = "\u00a0"; // Unicode code point for non-breaking space

    private NumberFormatterCurrency() { }

    // format the amount depend on the currencyCode
    public static NumberFormat getNumberFormat(@Nonnull final String currencyCode) {
        LOGGER.debug("### getting number format with currency code : {}", currencyCode);
        final Locale[] locales = NumberFormat.getAvailableLocales();

        for (final Locale locale : locales) {

            final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

            if (numberFormat.getCurrency().getCurrencyCode().equals(currencyCode)) {
                if (numberFormat instanceof DecimalFormat df) {
                    final DecimalFormatSymbols decimalFormatSymbols = df.getDecimalFormatSymbols();
                    decimalFormatSymbols.setCurrencySymbol("");
                    df.setDecimalFormatSymbols(decimalFormatSymbols);
                    return df;
                }
            }
        }

        return null;
    }

    // this for remove the character space of amount
    public static String getNumberFormat(@Nullable final String currencyCode, @Nullable final Double amount) {

        LOGGER.debug("## getNumberFormat with currency code :{} and amount :{}", currencyCode, amount);

        if (amount == null) {
            return "";
        }

        if (currencyCode == null || currencyCode.isEmpty()) {
            return amount.toString();
        }

        try {
            final NumberFormat fmt = getNumberFormat(currencyCode);
            if (fmt != null) {
                return fmt.format(amount).replace(NBSP, " ");
            } else {
                return "";
            }
        } catch (final Exception e) {
            LOGGER.warn("Number formatting exception: {}", e.getMessage());
            return "";
        }
    }
}
