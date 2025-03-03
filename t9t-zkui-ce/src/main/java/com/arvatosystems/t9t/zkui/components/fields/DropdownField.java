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
package com.arvatosystems.t9t.zkui.components.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Combobox;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28BasicFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28Registry;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.meta.DataCategory;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public class DropdownField extends AbstractField<Combobox> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropdownField.class);
    private final IDropdown28BasicFactory<Combobox> factory;
    private final boolean isDb;

    @Override
    protected Combobox createComponent(String suffix) {
        final String format = desc.getProperties() != null ? desc.getProperties().get(Constants.UiFieldProperties.DROPDOWN_FORMAT) : null;
        return factory.createInstance(format);
    }

    @Override
    protected boolean componentEmpty(Combobox c) {
        return c.getValue() == null || c.getValue().length() == 0;
    }

    @Override
    public SearchFilter getSearchFilter() {
        if (empty())
            return null;
        final DataCategory dataCategory = desc.getDataCategory();
        final Combobox cb = components.get(0);
        final String v = cb.getValue();
        if (isDb) {
            final Description rec = ((Dropdown28Db) cb).lookupById(v);
            LOGGER.debug("Text {} gives description {}", v, rec);
            if (rec == null) {
                return null; // no filter possible without a value to filter by
            }
            //          IDropdown28DbFactory dbFactory = (IDropdown28DbFactory)factory;
            if (dataCategory == DataCategory.NUMERIC || dataCategory == DataCategory.OBJECT || dataCategory == DataCategory.BASICNUMERIC) {
                // search by ref or integer
                final String javaType = desc.getDataType().toLowerCase();
                if (javaType.equals("int") || javaType.equals("integer")) {
                    final IntFilter f = new IntFilter();
                    f.setFieldName(getFieldName());
                    f.setEqualsValue(Integer.valueOf(rec.getId()));
                    return f;
                } else {
                    final LongFilter f = new LongFilter();
                    f.setFieldName(getFieldName());
                    f.setEqualsValue(rec.getObjectRef());
                    return f;
                }
            }
            // text only, but use displayId
            final UnicodeFilter f = new UnicodeFilter();
            f.setFieldName(getFieldName());
            if (isMultiDropdown()) {
                f.setLikeValue("%" + rec.getId() + "%");
            } else {
                f.setEqualsValue(rec.getId());
            }
            return f;
        }
        // text only
        final UnicodeFilter f = new UnicodeFilter();
        f.setFieldName(getFieldName());
        if (cfg.getFilterType() != UIFilterType.EQUALITY || isMultiDropdown()) {
            f.setLikeValue("%" + v + "%");
        } else {
            f.setEqualsValue(v);
        }
        return f;
    }

    public DropdownField(final String fieldname, final UIFilter cfg, final FieldDefinition desc, final String gridId, final ApplicationSession session, final String dropdownType) {
        super(fieldname, cfg, desc, gridId, session);
        if (cfg.getFilterType() != UIFilterType.EQUALITY && cfg.getFilterType() != UIFilterType.LIKE) {
            LOGGER.error("dropdown {} must have equality or LIKE constraint, but has {}", cfg.getFieldName(), cfg.getFilterType());
            throw new RuntimeException("dropdown " + cfg.getFieldName() + " must have equality or LIKE constraint, but has " + cfg.getFilterType());
        }
        factory = Dropdown28Registry.getFactoryById(dropdownType);
        if (factory == null) {
            LOGGER.warn("API specified a dropdown of type {} for {}, but it does not exist", dropdownType, cfg.getFieldName());
            throw new RuntimeException("unknown dropdown " + dropdownType);
        }
        createComponents();
        isDb = factory instanceof IDropdown28DbFactory;
    }

    @Override
    public void clear() {
        for (final Combobox e : components) {
            e.setValue(null);
        }
    }

    protected boolean isMultiDropdown() {
        return desc.getProperties() != null && desc.getProperties().containsKey(Constants.UiFieldProperties.MULTI_DROPDOWN);
    }
}
