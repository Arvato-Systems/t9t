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

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.enums.TokenizableEnum;
import de.jpaw.enums.XEnum;

/**
 * Abstract base class for dropdown data fields to eliminate duplicate code.
 * Provides common functionality for all dropdown-based data fields.
 *
 * @param <T> the type of the dropdown component's data
 * @param <V> the type of the field value
 */
public abstract class AbstractDropdownDataField<T extends BonaPortable, V> extends AbstractDataField<Dropdown28Db<T>, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDropdownDataField.class);

    protected final Dropdown28Db<T> c;
    protected final IDropdown28DbFactory<T> factory;
    protected final String filterFieldName;
    protected final String filterFieldName2;

    protected AbstractDropdownDataField(final DataFieldParameters params, final IDropdown28DbFactory<T> dbFactory) {
        super(params);
        factory = dbFactory;
        final String format;
        if (params.cfg != null && params.cfg.getProperties() != null) {
            format = params.cfg.getProperties().get(Constants.UiFieldProperties.DROPDOWN_FORMAT);
            filterFieldName = params.cfg.getProperties().get(Constants.UiFieldProperties.DROPDOWN_FILTER_FIELD);
            filterFieldName2 = params.cfg.getProperties().get(Constants.UiFieldProperties.DROPDOWN_FILTER_FIELD2);
        } else {
            format = null;
            filterFieldName = null;
            filterFieldName2 = null;
        }
        c = dbFactory.createInstance(format);
        configureComponent(c);
    }

    /**
     * Hook method for subclasses to configure the component after creation.
     * Default implementation sets constraints only.
     * @param component the dropdown component to configure
     */
    protected void configureComponent(final Dropdown28Db<T> component) {
        setConstraints(component, null);
    }

    @Override
    public boolean empty() {
        return c.getValue() == null;
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
    public V getValue() {
        final String res1 = c.getValue();
        final Comboitem res = c.getSelectedItem();

        LOGGER.debug("getValue({}) called, value is {}, item is {}: {}",
                getFieldName(),
                res1,
                res == null ? "NULL" : res.getClass().getCanonicalName(), res);
        if (res1 == null)
            return null;
        final Description desc = c.lookupById(res1);
        return desc == null ? null : extractValue(desc);
    }

    @Override
    public void setValue(final V data) {
        final Description desc = data == null ? null : lookupDescription(data);
        LOGGER.debug("{}.setValue(): setting {} results in {}", getFieldName(), data, desc);
        c.setValue(desc == null ? null : c.getFormattedLabel(desc));
    }

    /**
     * Returns the name of the primary field to use for filtering the dropdown, or null if no filter field is specified.
     */
    public String getFilterFieldName() {
        return filterFieldName;
    }

    /**
     * Returns the name of the secondary field to use for filtering the dropdown, or null if no filter field is specified.
     */
    public String getFilterFieldName2() {
        return filterFieldName2;
    }

    /**
     * Sets the primary filter on the dropdown based on a field value.
     * @param fieldValue the value to filter by
     */
    public void setFilterValue(final Object fieldValue) {
        c.setAdditionalFilter(makeFilter(filterFieldName, fieldValue));
    }

    /**
     * Sets the secondary filter on the dropdown based on a field value.
     * @param fieldValue the value to filter by
     */
    public void setFilterValue2(final Object fieldValue) {
        c.setAdditionalFilter2(makeFilter(filterFieldName2, fieldValue));
    }

    private SearchFilter makeFilter(final String field, final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return SearchFilters.equalsFilter(field, stringValue);
        } else if (value instanceof Long longValue) {
            return SearchFilters.equalsFilter(field, longValue);
        } else if (value instanceof Integer intValue) {
            return SearchFilters.equalsFilter(field, intValue);
        } else if (value instanceof TokenizableEnum alphaEnumValue) {
            return SearchFilters.equalsFilter(field, alphaEnumValue.getToken());
        } else if (value instanceof Enum<?> enumValue) {
            return SearchFilters.equalsFilter(field, enumValue.ordinal());
        } else if (value instanceof XEnum<?> xenumValue) {
            return SearchFilters.equalsFilter(field, xenumValue.getToken());
        } else if (value instanceof LocalDate dayValue) {
            return SearchFilters.equalsFilter(field, dayValue);
        } else {
            LOGGER.warn("Unsupported filter value type for field {}: {}", field, value.getClass());
            return null;
        }
    }

    /**
     * Extracts the value from the Description object.
     * @param desc the Description object from the dropdown
     * @return the extracted value of type V
     */
    protected abstract V extractValue(Description desc);

    /**
     * Looks up the Description for the given data value.
     * @param data the data value to look up
     * @return the Description object or null if not found
     */
    protected abstract Description lookupDescription(V data);
}
