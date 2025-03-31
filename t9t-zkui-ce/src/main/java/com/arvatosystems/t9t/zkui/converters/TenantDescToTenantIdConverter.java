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
package com.arvatosystems.t9t.zkui.converters;

import java.util.List;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.authc.api.TenantDescription;

public class TenantDescToTenantIdConverter implements Converter<Object, Object, Combobox> {

    @Override
    public Object coerceToUi(Object beanProp, Combobox box, BindContext ctx) {
        String beanPropRef = null;
        if (beanProp instanceof TenantDescription) {
            beanPropRef = ((TenantDescription) beanProp).getTenantId();
        }

        List<Comboitem> listitems = box.getItems();

        for (Comboitem listItem : listitems) {
            TenantDescription tenantDescription = (TenantDescription)listItem.getValue();
            if (null != tenantDescription && tenantDescription.getTenantId().equals(beanPropRef)) {
                return listItem;
            }
        }
        return listitems.isEmpty() ? null : listitems.get(0);
    }

    @Override
    public Object coerceToBean(Object compAttr, Combobox box, BindContext ctx) {
        if (box.getSelectedItem() == null) {
            return compAttr;
        } else {
            TenantDescription tenantDescription = (TenantDescription)box.getSelectedItem().getValue();
            return tenantDescription.getTenantId();
        }
    }
}
