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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.tfi.component.dropdown.Dropdown28Db;
import com.arvatosystems.t9t.tfi.component.dropdown.IDropdown28DbFactory;
import com.arvatosystems.t9t.base.search.Description;

import de.jpaw.bonaparte.core.BonaPortable;

public class DropdownDataField<T extends BonaPortable> extends AbstractDataField<Dropdown28Db<T>, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropdownDataField.class);

    protected final Dropdown28Db<T> c;
    protected final IDropdown28DbFactory<T> factory;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public DropdownDataField(DataFieldParameters params, String dropdownType, IDropdown28DbFactory<T> dbFactory) {
        super(params);
        factory = dbFactory;
        c = dbFactory.createInstance();
        setConstraints(c, null);
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Dropdown28Db<T> getComponent() {
        return c;
    }

    @Override
    public T getValue() {
        String res1 = c.getValue();
        Comboitem res = c.getSelectedItem();

        LOGGER.debug("getValue({}) called, value is {}, item is {}: {}",
            getFieldName(),
            res1,
            res == null ? "NULL" : res.getClass().getCanonicalName(), res);
        if (res1 == null)
            return null;
        Description desc = c.lookupById(res1);
        return desc == null ? null : factory.createRef(desc.getObjectRef());
    }

    @Override
    public void setValue(T data) {
        String id = data == null ? null : factory.getIdFromData(data, c);
        LOGGER.debug("{}.setValue(): setting {} results in {}", getFieldName(), data, id);
        c.setValue(id);
    }
}
