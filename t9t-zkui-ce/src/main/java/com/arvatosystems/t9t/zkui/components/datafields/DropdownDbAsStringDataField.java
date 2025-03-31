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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.pojos.apiw.Ref;

public class DropdownDbAsStringDataField extends AbstractDataField<Dropdown28Db<Ref>, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropdownDbAsStringDataField.class);

    protected final Dropdown28Db<Ref> c;
    protected final IDropdown28DbFactory<Ref> factory;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public DropdownDbAsStringDataField(final DataFieldParameters params, final String dropdownType, final IDropdown28DbFactory<Ref> dbFactory) {
        super(params);
        factory = dbFactory;
        final String format = params.cfg != null && params.cfg.getProperties() != null
                ? params.cfg.getProperties().get(Constants.UiFieldProperties.DROPDOWN_FORMAT)
                        : null;
        c = dbFactory.createInstance(format);
        setConstraints(c, null);
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Dropdown28Db<Ref> getComponent() {
        return c;
    }

    @Override
    public String getValue() {
        final String res1 = c.getValue(); // is label
        final Comboitem res = c.getSelectedItem();

        LOGGER.debug("getValue({}) called, value is {}, item is {}: {}",
                getFieldName(),
                res1,
                res == null ? "NULL" : res.getClass().getCanonicalName(), res);
        if (res1 == null)
            return null;
        final Description desc = c.lookupById(res1);
        return desc == null ? null : desc.getId();
    }

    @Override
    public void setValue(final String data) {
        final Description desc = data == null ? null : c.lookupByKey(data);
        LOGGER.debug("{}.setValue(): setting {} results in {}", getFieldName(), data, desc);
        c.setValue(desc == null ? null : c.getFormattedLabel(desc));
    }
}
