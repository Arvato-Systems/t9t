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
package com.arvatosystems.t9t.zkui.inputelements;

import java.math.RoundingMode;

import org.zkoss.zk.ui.WrongValueException;

import com.arvatosystems.t9t.zkui.util.CurrencyUtil;

import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsBox extends Fixedpointbox<MicroUnits, MicroUnitsBox> {
    private static final long serialVersionUID = 437573760456243476L;
    protected int decimals = 2; // default to 2 for fields that not generated from data field factory

    public MicroUnitsBox() {
        super(s -> MicroUnits.valueOf(s));
    }

    public MicroUnitsBox(MicroUnits value) throws WrongValueException {
        this();
        setValue(value);
    }

    @Override
    public MicroUnits getValue() {
        MicroUnits num = super.getValue();
        if (num == null)
            return null;
        // we cannot set a scale, but we can implement a rounding which matches that
        return num.round(decimals, RoundingMode.HALF_EVEN);
    }

    /**
     * fallback for setting the decimal without field definition
     */
    public void setDecimals(String currency) {
        setDecimals(currency, null);
    }

    /**
     * Set the decimals
     */
    public void setDecimals(String currency, FieldDefinition cfg) {
        String fieldName = this.getId();
        if (cfg != null) {
            decimals = ((BasicNumericElementaryDataItem)cfg).getDecimalDigits();
            fieldName = cfg.getName();
        }
        final Integer digits = CurrencyUtil.getFractionalDigits(currency, fieldName);
        if (digits != null) {
            // specific number of digits defined for this field
            decimals = digits;
            // refresh the value with new decimals
            setValue(getValue().toString());
        }
    }
}
