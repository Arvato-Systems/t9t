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

import org.zkoss.zul.Combobox;

import com.arvatosystems.t9t.tfi.component.dropdown.Dropdown28FactoryForQualifiers;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public class QualifierSelectionField extends AbstractField<Combobox> {
    // private static final Logger LOGGER = LoggerFactory.getLogger(QualifierSelectionField.class);
    private final String qualifierFor;

    @Override
    protected Combobox createComponent(String suffix) {
        return Dropdown28FactoryForQualifiers.createInstance(qualifierFor);
    }

    @Override
    protected boolean componentEmpty(Combobox c) {
        return c.getValue() == null || c.getValue().length() == 0;
    }

    @Override
    public SearchFilter getSearchFilter() {
        if (empty())
            return null;
        Combobox cb = components.get(0);
        String v = cb.getValue();
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

    public QualifierSelectionField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session, String qualifierFor) {
        super(fieldname, cfg, desc, gridId, session);
        this.qualifierFor = qualifierFor;
        createComponents();
    }

    @Override
    public void clear() {
        for (Combobox e : components)
            e.setValue(null);
    }
}
