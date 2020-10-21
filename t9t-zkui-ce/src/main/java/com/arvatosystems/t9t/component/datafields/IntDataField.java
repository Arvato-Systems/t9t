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

import org.zkoss.zul.Intbox;

import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;

public class IntDataField extends AbstractDataField<Intbox, Integer> {
    protected final Intbox c = new Intbox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public IntDataField(DataFieldParameters params) {
        super(params);
        BasicNumericElementaryDataItem cfg2 = (BasicNumericElementaryDataItem)params.cfg;
        setConstraints(c, cfg2.getIsSigned() ? null : "no negative");
        c.setMaxlength(cfg2.getTotalDigits());
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Intbox getComponent() {
        return c;
    }

    @Override
    public Integer getValue() {
        return c.getValue();
    }

    @Override
    public void setValue(Integer data) {
        c.setValue(data);
    }
}
