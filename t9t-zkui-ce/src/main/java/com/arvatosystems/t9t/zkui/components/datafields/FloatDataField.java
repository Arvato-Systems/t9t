/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import org.zkoss.zul.Doublebox;

import com.arvatosystems.t9t.base.T9tUtil;

import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;

public class FloatDataField extends AbstractDataField<Doublebox, Float> {
    protected final Doublebox c = new Doublebox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public FloatDataField(DataFieldParameters params) {
        super(params);
        BasicNumericElementaryDataItem cfg2 = (BasicNumericElementaryDataItem)params.cfg;
        setConstraints(c, cfg2.getIsSigned() ? null : "no negative");
        c.setMaxlength(20);
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Doublebox getComponent() {
        return c;
    }

    @Override
    public Float getValue() {
        return T9tUtil.asFloat(c.getValue());
    }

    @Override
    public void setValue(Float data) {
        c.setValue(T9tUtil.asDouble(data));
    }
}
