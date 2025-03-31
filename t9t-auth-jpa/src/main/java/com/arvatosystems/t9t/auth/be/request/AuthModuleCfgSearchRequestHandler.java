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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.jpa.entities.AuthModuleCfgEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IAuthModuleCfgDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IAuthModuleCfgEntityResolver;
import com.arvatosystems.t9t.auth.request.AuthModuleCfgSearchRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class AuthModuleCfgSearchRequestHandler extends
  AbstractSearchWithTotalsRequestHandler<String, AuthModuleCfgDTO, FullTrackingWithVersion, AuthModuleCfgSearchRequest, AuthModuleCfgEntity> {

    private final IAuthModuleCfgEntityResolver resolver = Jdp.getRequired(IAuthModuleCfgEntityResolver.class);
    private final IAuthModuleCfgDTOMapper mapper = Jdp.getRequired(IAuthModuleCfgDTOMapper.class);

    @Override
    public ReadAllResponse<AuthModuleCfgDTO, FullTrackingWithVersion>
      execute(final RequestContext ctx, final AuthModuleCfgSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
