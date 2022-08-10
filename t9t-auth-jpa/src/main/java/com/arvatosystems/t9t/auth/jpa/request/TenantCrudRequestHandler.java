/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.jpa.request;

import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.ITenantDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.ITenantEntityResolver;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudStringKeyRequestHandler;
import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;

public class TenantCrudRequestHandler extends AbstractCrudStringKeyRequestHandler<TenantDTO, FullTrackingWithVersion, TenantCrudRequest, TenantEntity> {

    protected final ITenantEntityResolver resolver = Jdp.getRequired(ITenantEntityResolver.class);
    protected final ITenantDTOMapper mapper = Jdp.getRequired(ITenantDTOMapper.class);
    protected final IAuthCacheInvalidation cacheInvalidator = Jdp.getRequired(IAuthCacheInvalidation.class);

    @Override
    public CrudStringKeyResponse<TenantDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final TenantCrudRequest crudRequest) {
        final CrudStringKeyResponse<TenantDTO, FullTrackingWithVersion> result = super.execute(ctx, mapper, resolver, crudRequest);
        if (crudRequest.getCrud() != OperationType.READ) {
            final String tenantId = result.getData() != null ? result.getData().getTenantId() : null;
            cacheInvalidator.invalidateAuthCache(ctx, TenantDTO.class.getSimpleName(), null, tenantId);
        }
        return result;
    }
}
