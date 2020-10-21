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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.T9tConstants;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public class TenantField extends AbstractField<Combobox> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantField.class);
    private final Combobox cb = new Combobox();

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

    @Override
    public SearchFilter getSearchFilter() {
        Comboitem ci = cb.getSelectedItem();
        if (ci == null || empty())
            return null;
        LongFilter f = new LongFilter();
        f.setFieldName(getFieldName());
        f.setEqualsValue(ci.getValue());
        return f;
    }

    public TenantField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session, String tenantCategory) {
        super(fieldname, cfg, desc, gridId, session);
        if (cfg.getFilterType() != UIFilterType.EQUALITY) {
            throw new RuntimeException("tenant combobox must have equality constraint");
        }
        if (tenantCategory == null || tenantCategory.equals("I")) {
            tenantCategory = "I";
            LOGGER.error("Tenant selection for a tenant isolated DTO does not make sense!");
        }
        createComponents();

        if (tenantCategory == "E") {
            // E is A for @, D for any other tenant
            tenantCategory = session.getTenantId().equals(T9tConstants.GLOBAL_TENANT_ID) ? "A" : "D";
        }

        switch (tenantCategory) {
        case "A":
            if (session.getTenantId().equals(T9tConstants.GLOBAL_TENANT_ID)) {
                // get all allowed tenants
                List<TenantDescription> allowedTenants = session.getAllowedTenants();
                for (TenantDescription td : allowedTenants) {
                    newTenantComboItem(cb, td.getTenantRef(), td.getTenantId());
                }
            } else {
                // only own tenant
                newTenantComboItem(cb, session.getTenantRef(), session.getTenantId());
            }
            break;
        case "D":
            newTenantComboItem(cb, T9tConstants.GLOBAL_TENANT_REF42, T9tConstants.GLOBAL_TENANT_ID);
            if (T9tConstants.GLOBAL_TENANT_REF42.equals(session.getTenantRef()))
                break;  // do not show the same twice
            // fall through
        default:
            newTenantComboItem(cb, session.getTenantRef(), session.getTenantId());
        }
    }

    @Override
    public void clear() {
        for (Combobox e : components)
            e.setValue(null);
    }


    private static void newTenantComboItem(Combobox cb, Long ref, String id) {
        Comboitem ci = new Comboitem();
        ci.setLabel(id);
        ci.setValue(ref);
        ci.setParent(cb);
    }
}
