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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.ApiKeyRef;
import com.arvatosystems.t9t.auth.jpa.entities.ApiKeyEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IApiKeyDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IApiKeyEntityResolver;
import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class ApiKeyCrudRequestHandler extends AbstractCrudSurrogateKey42RequestHandler  <ApiKeyRef, ApiKeyDTO, FullTrackingWithVersion, ApiKeyCrudRequest, ApiKeyEntity> {

    private final IApiKeyDTOMapper mapper = Jdp.getRequired(IApiKeyDTOMapper.class);

    private final IApiKeyEntityResolver resolver = Jdp.getRequired(IApiKeyEntityResolver.class);

    @Override
    public CrudSurrogateKeyResponse<ApiKeyDTO, FullTrackingWithVersion> execute(RequestContext ctx, ApiKeyCrudRequest crudRequest) {
        return execute(ctx, mapper, resolver, crudRequest);
    }
}
