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

import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.zkui.IKeyFromCrudRequest;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("tenant")
public class TenantKeyFromCrudRequest implements IKeyFromCrudRequest<TenantDTO, FullTrackingWithVersion, TenantCrudRequest> {

    @Override
    public SearchFilter getFilterForKey(final TenantCrudRequest crudRequest) {
        final UnicodeFilter tenantFilter = new UnicodeFilter(TenantDTO.meta$$tenantId.getName());
        tenantFilter.setEqualsValue(crudRequest.getKey());
        return tenantFilter;
    }
}
