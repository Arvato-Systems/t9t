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
package com.arvatosystems.t9t.zkui.viewmodel.keyProvider;

import com.arvatosystems.t9t.auth.UserTenantRoleDTO;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.zkui.IKeyFromDataProvider;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("userTenantRole")
public class UserTenantRoleKeyFromDataProvider implements IKeyFromDataProvider<UserTenantRoleDTO, TrackingBase> {

    @Override
    public SearchFilter getFilterForKey(DataWithTracking<UserTenantRoleDTO, TrackingBase> dwt) {
        final UnicodeFilter tenantFilter = new UnicodeFilter(T9tConstants.TENANT_ID_FIELD_NAME);
        tenantFilter.setEqualsValue(((DataWithTrackingS<UserTenantRoleDTO, TrackingBase>)dwt).getTenantId());

        final LongFilter roleFilter = new LongFilter("roleRef");
        roleFilter.setEqualsValue(dwt.getData().getRoleRef().getObjectRef());

        final LongFilter userFilter = new LongFilter("userRef");
        userFilter.setEqualsValue(dwt.getData().getUserRef().getObjectRef());

        return SearchFilters.and(tenantFilter, SearchFilters.and(roleFilter, userFilter));
    }
}
