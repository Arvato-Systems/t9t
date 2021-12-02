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
package com.arvatosystems.t9t.zkui.components.datafields;

import java.math.RoundingMode;

import com.arvatosystems.t9t.zkui.inputelements.MicroUnitsBox;
import com.arvatosystems.t9t.zkui.util.CurrencyUtil;

import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;
import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsDataField extends AbstractDataField<MicroUnitsBox, MicroUnits> {
    protected final MicroUnitsBox c = new MicroUnitsBox();
    protected int decimals;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public MicroUnitsDataField(DataFieldParameters params) {
        super(params);
        BasicNumericElementaryDataItem cfg2 = (BasicNumericElementaryDataItem) params.cfg;
        setConstraints(c, cfg2.getIsSigned() ? null : "no negative");
        c.setMaxlength(cfg2.getTotalDigits() + 2); // 2 chars for sign and decimal point
        setDecimals(params.decimals);
    }

    @Override
    public void clear() {
        c.setValue("");
    }

    @Override
    public MicroUnitsBox getComponent() {
        return c;
    }

    @Override
    public void setValue(MicroUnits data) {
        c.setValue(data.toString());
    }

    @Override
    public MicroUnits getValue() {
        MicroUnits num = c.getValue();
        if (num == null)
            return null;
        // we cannot set a scale, but we can implement a rounding which matches that
        return num.round(decimals, RoundingMode.HALF_EVEN);
    }

    public void setDecimals(String currency) {
        decimals = ((BasicNumericElementaryDataItem)cfg).getDecimalDigits();
        final Integer digits = CurrencyUtil.getFractionalDigits(currency, cfg.getName());
        if (digits != null && digits <= decimals) {
            // specific number of digits defined for this field
            decimals = digits;
        }
        // c.setScale(decimals);
    }
}
