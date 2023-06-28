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
package com.arvatosystems.t9t.zkui.converters;

import java.util.List;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;
import com.google.common.base.Strings;

import de.jpaw.bonaparte.pojos.apiw.Ref;

public class DropdownConverter implements Converter<Object, Object, Dropdown28Db<?>> {

    @Override
    public Object coerceToUi(Object ref, Dropdown28Db<?> dropdown28Db, BindContext ctx) {

        Description description = ref == null ? null : dropdown28Db.lookupByRef(((Ref)ref).getObjectRef());  // FIXME:is this always a Ref?

        List<Comboitem> listitems = dropdown28Db.getItems();

        if (null != description) {
            for (Comboitem listItem : listitems) {
                if (null != listItem.getValue() && listItem.getValue().equals(description.getId())) {
                    return listItem;
                }
            }
        }

        if (!listitems.isEmpty() && null != description) {
            dropdown28Db.removeItemAt(dropdown28Db.getItemCount() - 1);
            dropdown28Db.appendItem(description.getId());
            return dropdown28Db.getItemAtIndex(dropdown28Db.getItemCount() - 1);
        }

        return listitems.isEmpty() || null == description ? null : listitems.get(0);

    }

    @Override
    public Object coerceToBean(Object compAttr, Dropdown28Db<?> dropdown28Db, BindContext ctx) {

        String current = dropdown28Db.getValue();
        if (Strings.isNullOrEmpty(current))
            return null;

        Description d = dropdown28Db.lookupById(current.toLowerCase());
        if (d == null)
            return null;

        Object r = dropdown28Db.getFactory().createKey(d);

        if (dropdown28Db.getSelectedItem() == null) {
            return compAttr;
        } else {
            return r;
        }
    }
}
