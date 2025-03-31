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
package com.arvatosystems.t9t.zkui.components.datafields;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.zkoss.zul.Decimalbox;

import com.arvatosystems.t9t.zkui.util.CurrencyUtil;

import de.jpaw.bonaparte.pojos.meta.NumericElementaryDataItem;

public class DecimalDataField extends AbstractDataField<Decimalbox, BigDecimal> {
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
        decimals = ((NumericElementaryDataItem)cfg).getDecimalDigits();
        final Integer digits = CurrencyUtil.getFractionalDigits(currency, cfg.getName());
        if (digits != null && digits <= decimals) {
            // specific number of digits defined for this field
            decimals = digits;
        }
        c.setScale(decimals);
    }
}
