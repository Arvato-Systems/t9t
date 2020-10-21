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
package com.arvatosystems.t9t.tfi.web;

import java.util.List;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.auth.TenantRef;
import com.arvatosystems.t9t.authc.api.TenantDescription;

public class TenantDescToTenantRefConverter implements Converter<Object, Object, Component>{

    @Override
    public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
        Combobox box = (Combobox) component;
        Long  beanPropRef = null;
        if (beanProp instanceof TenantDescription) {
            beanPropRef=((TenantDescription)beanProp).getTenantRef();
        }

        List<Comboitem> listitems = box.getItems();

        for (Comboitem listItem : listitems) {
            TenantDescription tenantDescription = (TenantDescription)listItem.getValue();
            //if (/*listItem.getValue()*/dataWithTrackingW.getData().equals(beanProp)) {
            //if (null!=listItem.getValue() && listItem.getValue().equals(beanProp)) {
            if (null!=tenantDescription && tenantDescription.getTenantRef().equals(beanPropRef)) {
                return listItem;
            }
        }
        return listitems.isEmpty() ? null : listitems.get(0);
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {

        Combobox box;
        box = (Combobox) component;

        if (box.getSelectedItem() == null) {
            return compAttr;
        } else {
            TenantDescription tenantDescription = (TenantDescription)box.getSelectedItem().getValue();
            TenantRef tenantRef= new TenantRef(tenantDescription.getTenantRef());
            return tenantRef;
        }
    }
}
