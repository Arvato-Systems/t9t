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

import com.arvatosystems.t9t.auth.TenantLogoDTO;
import com.arvatosystems.t9t.auth.jpa.entities.TenantLogoEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.ITenantLogoDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.ITenantLogoEntityResolver;
import com.arvatosystems.t9t.auth.request.TenantLogoSearchRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class TenantLogoSearchRequestHandler extends
  AbstractSearch42RequestHandler<Long, TenantLogoDTO, FullTrackingWithVersion, TenantLogoSearchRequest, TenantLogoEntity> {

    protected final ITenantLogoEntityResolver resolver = Jdp.getRequired(ITenantLogoEntityResolver.class);
    protected final ITenantLogoDTOMapper mapper = Jdp.getRequired(ITenantLogoDTOMapper.class);

    @Override
    public ReadAllResponse<TenantLogoDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final TenantLogoSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}