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

import com.arvatosystems.t9t.tfi.component.Dropdown28Ext;
import com.arvatosystems.t9t.tfi.component.dropdown.Dropdown28FactoryForQualifiers;
import com.arvatosystems.t9t.tfi.component.dropdown.IDropdown28BasicFactory;

/** Prebuilt dropdowns. */
public class DropdownBasicDataField extends AbstractDataField<Dropdown28Ext, String> {
    // private static final Logger LOGGER = LoggerFactory.getLogger(DropdownBasicDataField.class);

    protected final Dropdown28Ext c;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public DropdownBasicDataField(DataFieldParameters params, String dropdownType, IDropdown28BasicFactory<Dropdown28Ext> dbFactory) {
        super(params);
        c = dbFactory.createInstance();
        setConstraints(c, null);
    }

    public DropdownBasicDataField(DataFieldParameters params, String qualifierFor) {
        super(params);
        c = Dropdown28FactoryForQualifiers.createInstance(qualifierFor);
        setConstraints(c, null);
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Dropdown28Ext getComponent() {
        return c;
    }

    @Override
    public String getValue() {
        return c.getValue();
    }

    @Override
    public void setValue(String data) {
        c.setValue(data);
    }
}
