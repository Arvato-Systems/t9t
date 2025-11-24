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
package com.arvatosystems.t9t.zkui.components.fields;

import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;

public abstract class EnumBaseField extends AbstractField<Combobox> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumBaseField.class);
    protected final Combobox cb = new Combobox();
    protected final Predicate<String> enumRestrictions;

    @Override
    protected Combobox createComponent(String suffix) {
        cb.setRows(1);
        cb.setMold("default");
        return cb;
    }

    @Override
    protected boolean componentEmpty(Combobox c) {
        return c.getValue() == null || c.getValue().length() == 0;
    }

    public EnumBaseField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session, String pqon) {
        super(fieldname, cfg, desc, gridId, session);

        LOGGER.debug("EnumBaseField called with fieldname={}, gridId={} and pqon={}", fieldname, gridId, pqon);

        if (pqon == null) {
            this.enumRestrictions = ApplicationSession.NO_ENUM_RESTRICTION;  // boolean fields cannot be restricted
        } else {
            final String enumDtoRestriction = desc.getProperties() != null ? desc.getProperties().get(Constants.UiFieldProperties.ENUMS) : null;
            this.enumRestrictions = session.enumRestrictions(pqon, enumDtoRestriction, null);
        }
    }

    @Override
    public void clear() {
        for (Combobox e : components) {
            e.setValue(null);
        }
    }

    protected void createComp(EnumDefinition ed, ApplicationSession as) {
        createComponents();
        Map<String, String> translations = as.translateEnum(ed.getName());
        for (String s : ed.getIds()) {
            if (enumRestrictions.test(s)) {
                String xlation = translations.get(s);
                newComboItem(s, xlation == null ? s : xlation);
            }
        }
    }

    protected void newComboItem(String value, String text) {
        Comboitem ci = new Comboitem();
        ci.setLabel(text);
        ci.setValue(value);
        ci.setParent(cb);
    }
}
