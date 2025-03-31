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

import com.arvatosystems.t9t.zkui.components.dropdown28.factories.Dropdown28FactoryForQualifiers;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28ComboBoxItem;

public class DropdownComboBoxItemDataField extends AbstractDataField<Dropdown28ComboBoxItem, String> {
    protected final Dropdown28ComboBoxItem c;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public DropdownComboBoxItemDataField(DataFieldParameters params, String qualifierFor) {
        super(params);
        c = Dropdown28FactoryForQualifiers.createInstance(qualifierFor);
        setConstraints(c, null);
    }

    @Override
    public void clear() {
        c.setSelectedItem(null);
    }

    @Override
    public Dropdown28ComboBoxItem getComponent() {
        return c;
    }

    @Override
    public String getValue() {
        return c.getSelectedItem() == null ? null : c.getSelectedItem().getValue();
    }

    @Override
    public void setValue(String value) {
        if (value == null) {
            clear();
        } else {
            c.setSelectedItem(c.getComboItemByValue(value));
        }
    }
}
