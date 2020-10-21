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

import java.util.UUID;

import org.zkoss.zul.Textbox;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;


public class UuidField extends AbstractField<Textbox> {
    @Override
    protected Textbox createComponent(String suffix) {
        Textbox textbox = new Textbox();
        textbox.setId(cfg.getFieldName() + suffix);
        textbox.setHflex("1");
        textbox.setPlaceholder(label);
        return textbox;
    }

    @Override
    protected boolean componentEmpty(Textbox c) {
        String s = c.getValue();
        return s == null || s.length() == 0;
    }

    @Override
    public SearchFilter getSearchFilter() {
        if (empty())
            return null;
        // depending on which values are set, create a lower, upper, equals or range filter
        UuidFilter f = new UuidFilter();
        f.setFieldName(getFieldName());
        String v = components.get(0).getValue();
        switch (cfg.getFilterType()) {
        case EQUALITY:
            f.setEqualsValue(UUID.fromString(v));
            break;
        default:
            break;
        }
        return f;
    }

    public UuidField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) {
        super(fieldname, cfg, desc, gridId, session);
        if (cfg.getFilterType() != UIFilterType.EQUALITY) {
            throw new RuntimeException("UUID filter must have equality constraint: " + fieldname);
        }
        createComponents();
    }

    @Override
    public void clear() {
        for (Textbox e : components)
            e.setValue(null);
    }
}
