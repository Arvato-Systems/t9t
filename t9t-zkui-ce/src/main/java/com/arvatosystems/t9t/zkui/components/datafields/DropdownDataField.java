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

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;

import de.jpaw.bonaparte.core.BonaPortable;

public class DropdownDataField<T extends BonaPortable> extends AbstractDropdownDataField<T, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropdownDataField.class);

    public DropdownDataField(final DataFieldParameters params, final String dropdownType, final IDropdown28DbFactory<T> dbFactory) {
        super(params, dbFactory);
    }

    @Override
    protected void configureComponent(final Dropdown28Db<T> component) {
        component.setHflex("1");
        setConstraints(component, null);
    }

    @Override
    protected T extractValue(final Description desc) {
        return factory.createRef(desc.getObjectRef());
    }

    @Override
    protected Description lookupDescription(final T data) {
        final String id = factory.getIdFromData(data, c);
        LOGGER.debug("{}.lookupDescription(): data {} results in id {}", getFieldName(), data, id);
        // For DropdownDataField, we need to use setValue directly with the ID
        // This is handled specially by overriding setValue
        return null;
    }

    @Override
    public void setValue(final T data) {
        final String id = data == null ? null : factory.getIdFromData(data, c);
        LOGGER.debug("{}.setValue(): setting {} results in {}", getFieldName(), data, id);
        c.setValue(id);
    }
}
