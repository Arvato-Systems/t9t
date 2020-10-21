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

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public class BinaryField extends TextField {
    public BinaryField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId,
            ApplicationSession session) {
        super(fieldname, cfg, desc, gridId, session);
        if (cfg.getFilterType() != UIFilterType.EQUALITY) {
            throw new RuntimeException("Binary filter must have equality constraint: " + fieldname);
        }
    }

    @Override
    public SearchFilter getSearchFilter() {
        String value = components.get(0).getValue();
        if (value == null || value.length() == 0)
            return null;
        // TODO: there is no BinaryFilter yet. But then, it is very unlikely that it will be required.
//        BinaryFilter f = new BinaryFilter();
//        f.setFieldName(getFieldName());
//        return f;
        return null;
    }
}
