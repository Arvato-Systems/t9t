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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.zkui.viewmodel.AbstractCrudVM;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

@Init(superclass = true)
public class TenantVM extends AbstractCrudVM<String, TenantDTO, FullTrackingWithVersion, TenantCrudRequest,
  CrudStringKeyResponse<TenantDTO, FullTrackingWithVersion>> {

    protected PermissionsDTO defaultTenantPermissions() {
        PermissionsDTO p = new PermissionsDTO();
        p.setMinPermissions(Permissionset.ofTokens());
        p.setMaxPermissions(new Permissionset(0xfffff));
        return p;
    }

    @Override
    protected void clearData() {
        super.clearData();
        data.setPermissions(defaultTenantPermissions());
    }

    @Override
    protected void loadData(DataWithTracking<TenantDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
        if (data.getPermissions() == null)
            data.setPermissions(defaultTenantPermissions());
    }

    @Override
    protected CrudAnyKeyRequest<TenantDTO, FullTrackingWithVersion> createCrudWithKey() {
        final TenantCrudRequest crudRq = new TenantCrudRequest();
        crudRq.setKey(data.getTenantId());
        return crudRq;
    }

    @Override
    protected void clearKey() {
        data.setTenantId(null);
    }
}
