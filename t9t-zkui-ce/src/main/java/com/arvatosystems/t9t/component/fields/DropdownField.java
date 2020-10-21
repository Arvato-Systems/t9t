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
package com.arvatosystems.t9t.component.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Combobox;

import com.arvatosystems.t9t.tfi.component.dropdown.Dropdown28Db;
import com.arvatosystems.t9t.tfi.component.dropdown.Dropdown28Registry;
import com.arvatosystems.t9t.tfi.component.dropdown.IDropdown28BasicFactory;
import com.arvatosystems.t9t.tfi.component.dropdown.IDropdown28DbFactory;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.search.Description;

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
        return factory.createInstance();
    }

    @Override
    protected boolean componentEmpty(Combobox c) {
        return c.getValue() == null || c.getValue().length() == 0;
    }

    @Override
    public SearchFilter getSearchFilter() {
        if (empty())
            return null;
        DataCategory dataCategory = desc.getDataCategory();
        String dataType = desc.getBonaparteType();
        Combobox cb = components.get(0);
        String v = cb.getValue();
        if (isDb) {
//          IDropdown28DbFactory dbFactory = (IDropdown28DbFactory)factory;
            if (dataCategory == DataCategory.NUMERIC || dataCategory == DataCategory.OBJECT || dataCategory == DataCategory.BASICNUMERIC) {
                // search by ref
                LongFilter f = new LongFilter();
                f.setFieldName(getFieldName());
                Description rec = ((Dropdown28Db)cb).lookupById(v);
                LOGGER.info("Text {} gives description {}", v, rec);
                f.setEqualsValue(rec == null ? null : rec.getObjectRef());
                return f;
            }
        }
        // text only
        UnicodeFilter f = new UnicodeFilter();
        f.setFieldName(getFieldName());
        if (cfg.getFilterType() != UIFilterType.EQUALITY) {
            f.setLikeValue(v);
        } else {
            f.setEqualsValue(v);
        }
        return f;
    }

    public DropdownField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session, String dropdownType) {
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
        for (Combobox e : components)
            e.setValue(null);
    }
}
