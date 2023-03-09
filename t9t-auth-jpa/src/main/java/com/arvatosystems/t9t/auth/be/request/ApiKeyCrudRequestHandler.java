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

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.ApiKeyRef;
import com.arvatosystems.t9t.auth.T9tAuthTools;
import com.arvatosystems.t9t.auth.jpa.entities.ApiKeyEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IApiKeyDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IApiKeyEntityResolver;
import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;

public class ApiKeyCrudRequestHandler extends
  AbstractCrudSurrogateKeyRequestHandler<ApiKeyRef, ApiKeyDTO, FullTrackingWithVersion, ApiKeyCrudRequest, ApiKeyEntity> {

    private final IApiKeyDTOMapper mapper = Jdp.getRequired(IApiKeyDTOMapper.class);
    private final IApiKeyEntityResolver resolver = Jdp.getRequired(IApiKeyEntityResolver.class);
    private final IAuthCacheInvalidation cacheInvalidator = Jdp.getRequired(IAuthCacheInvalidation.class);

    @Override
    public CrudSurrogateKeyResponse<ApiKeyDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final ApiKeyCrudRequest crudRequest) {
        final ApiKeyDTO apiKeyDto = crudRequest.getData();
        if (apiKeyDto != null) {
            // limit the min / max permissions
            final JwtInfo jwt = ctx.internalHeaderParameters.getJwtInfo();
            T9tAuthTools.maskPermissions(apiKeyDto.getPermissions(), jwt.getPermissionsMax());
        }

        final CrudSurrogateKeyResponse<ApiKeyDTO, FullTrackingWithVersion> result = execute(ctx, mapper, resolver, crudRequest);
        if (crudRequest.getCrud() != OperationType.READ) {
            final String apiKey = result.getData() != null ? result.getData().getApiKey().toString() : null;
            cacheInvalidator.invalidateAuthCache(ctx, ApiKeyDTO.class.getSimpleName(), result.getKey(), apiKey);
        }
        return result;
    }
}
