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
package com.arvatosystems.t9t.viewmodel;

import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.ApiKeyRef;
import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.components.crud.CrudSurrogateKeyVM;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;

@Init(superclass = true)
public class ApiKeyVM extends CrudSurrogateKeyVM<ApiKeyRef, ApiKeyDTO, FullTrackingWithVersion> {

    // min/maxPermissions should be null by default, which means inherit from user

    @Override
    protected void clearData() {
        super.clearData();
        if (data.getPermissions() == null)
            data.setPermissions(new PermissionsDTO());
    }

    @Override
    protected void loadData(DataWithTracking<ApiKeyDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
        if (data.getPermissions() == null)
            data.setPermissions(new PermissionsDTO());
    }
}
