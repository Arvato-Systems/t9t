/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.components.datafields;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.types.TenantIsolationCategoryType;

public class TenantDataField extends AbstractDataField<Combobox, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantDataField.class);
    private final Combobox c = new Combobox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }


    public TenantDataField(DataFieldParameters params, TenantIsolationCategoryType tenantCategory) {
        super(params);
        setConstraints(c, null);
        if (tenantCategory == null || tenantCategory == TenantIsolationCategoryType.ISOLATED) {
            tenantCategory = TenantIsolationCategoryType.ISOLATED;
            LOGGER.error("Tenant selection for a tenant isolated DTO does not make sense!");
        }

        if (tenantCategory == TenantIsolationCategoryType.ISOLATED_ADMIN_DEFAULT) {
            // E is A for @, D for any other tenant
            tenantCategory = as.getTenantId().equals(T9tConstants.GLOBAL_TENANT_ID)
              ? TenantIsolationCategoryType.ISOLATED_WITH_ADMIN
              : TenantIsolationCategoryType.ISOLATED_WITH_DEFAULT;
        }

        switch (tenantCategory) {
        case ISOLATED_WITH_ADMIN:
            // get all allowed tenants
            List<TenantDescription> allowedTenants = as.getAllowedTenants();
            for (TenantDescription td : allowedTenants) {
                newTenantComboItem(c, td.getTenantId());
            }
            break;
        case ISOLATED_WITH_DEFAULT:
            newTenantComboItem(c, T9tConstants.GLOBAL_TENANT_ID);
            if (T9tConstants.GLOBAL_TENANT_ID.equals(as.getTenantId()))
                break;  // do not show the same twice
            // fall through
        default:
            newTenantComboItem(c, as.getTenantId());
        }
        c.setReadonly(true);
    }


    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Combobox getComponent() {
        return c;
    }

    @Override
    public String getValue() {
        Comboitem ci = c.getSelectedItem();
        return ci == null ? null : ci.getValue();
    }

    @Override
    public void setValue(String data) {
        TenantDescription d = as.getTenantById(data);
        if (d == null)
            clear();
        else
            c.setValue(d.getTenantId());
    }

    private static void newTenantComboItem(Combobox cb, String id) {
        Comboitem ci = new Comboitem();
        ci.setLabel(id);
        ci.setValue(id);
        ci.setParent(cb);
    }
}
