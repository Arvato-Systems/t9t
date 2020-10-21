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

import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public class EnumField extends EnumBaseField {
    protected final EnumDefinition ed;

    @Override
    public SearchFilter getSearchFilter() {
        Comboitem ci = cb.getSelectedItem();
        if (ci == null || empty())
            return null;
        EnumFilter f = new EnumFilter();
        f.setFieldName(getFieldName());
        f.setEnumPqon(ed.getName());
        f.setEqualsName(ci.getValue());
        return f;
    }

    public EnumField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) {
        super(fieldname, cfg, desc, gridId, session, ((EnumDataItem)desc).getBaseEnum().getName());
        ed = ((EnumDataItem)desc).getBaseEnum();
        if (cfg.getFilterType() != UIFilterType.EQUALITY) {
            throw new RuntimeException("enum combobox must have equality constraint: " + fieldname);
        }
       createComp(ed, session);
    }
}
