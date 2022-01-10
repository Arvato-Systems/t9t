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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.TenantRef;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.auth.services.ITenantResolver;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;

public class TenantCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<TenantRef, TenantDTO, FullTrackingWithVersion, TenantCrudRequest> {

    protected final ITenantResolver resolver = Jdp.getRequired(ITenantResolver.class);
    protected final IAuthCacheInvalidation cacheInvalidator = Jdp.getRequired(IAuthCacheInvalidation.class);

    @Override
    public CrudSurrogateKeyResponse<TenantDTO, FullTrackingWithVersion> execute(RequestContext ctx, TenantCrudRequest crudRequest) {
        final CrudSurrogateKeyResponse<TenantDTO, FullTrackingWithVersion> result = execute(ctx, crudRequest, resolver);
        if (crudRequest.getCrud() != OperationType.READ) {
            final String tenantId = result.getData() != null ? result.getData().getTenantId() : null;
            cacheInvalidator.invalidateAuthCache(ctx, TenantDTO.class.getSimpleName(), result.getKey(), tenantId);
        }
        return result;
    }
}
