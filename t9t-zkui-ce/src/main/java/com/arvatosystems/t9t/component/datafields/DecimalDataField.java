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
package com.arvatosystems.t9t.component.datafields;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Decimalbox;

import de.jpaw.bonaparte.pojos.meta.NumericElementaryDataItem;
import de.jpaw.util.ExceptionUtil;

public class DecimalDataField extends AbstractDataField<Decimalbox, BigDecimal> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DecimalDataField.class);
    protected final Decimalbox c = new Decimalbox();
    protected int decimals;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public DecimalDataField(DataFieldParameters params) {
        super(params);
        NumericElementaryDataItem cfg2 = (NumericElementaryDataItem)params.cfg;
        setConstraints(c, cfg2.getIsSigned() ? null : "no negative");
        c.setMaxlength(cfg2.getTotalDigits() + 2); // 2 chars for sign and decimal point
        setDecimals(params.decimals);
    }

    @Override
    public void clear() {
        c.setValue("");
    }

    @Override
    public Decimalbox getComponent() {
        return c;
    }

    @Override
    public BigDecimal getValue() {
        BigDecimal num = c.getValue();
        if (num == null)
            return null;
        return num.setScale(decimals, RoundingMode.HALF_EVEN);
    }

    @Override
    public void setValue(BigDecimal data) {
        c.setValue(data);
    }

    public void setDecimals(String currency) {
        if (currency == null)
            decimals = ((NumericElementaryDataItem)cfg).getDecimalDigits();
        else if (currency.length() == 1 && Character.isDigit(currency.charAt(0)))
            decimals = Integer.valueOf(currency);
        else {
            // assume it is a currency
            try {
                Currency curr = Currency.getInstance(currency);
                decimals = curr.getDefaultFractionDigits();
            } catch (IllegalArgumentException e) {
                LOGGER.error("Cannot get currency for {}: {}", currency, ExceptionUtil.causeChain(e));
                decimals = 2; // most currencies have 2 decimals...
            }
        }
        c.setScale(decimals);
    }
}
