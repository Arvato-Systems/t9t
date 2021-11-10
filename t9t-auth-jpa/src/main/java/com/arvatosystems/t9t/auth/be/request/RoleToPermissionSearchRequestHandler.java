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

import com.arvatosystems.t9t.auth.RoleToPermissionDTO;
import com.arvatosystems.t9t.auth.RoleToPermissionInternalKey;
import com.arvatosystems.t9t.auth.jpa.entities.RoleToPermissionEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleToPermissionDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleToPermissionEntityResolver;
import com.arvatosystems.t9t.auth.request.RoleToPermissionSearchRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class RoleToPermissionSearchRequestHandler extends AbstractSearch42RequestHandler<RoleToPermissionInternalKey, RoleToPermissionDTO,
  FullTrackingWithVersion, RoleToPermissionSearchRequest, RoleToPermissionEntity> {

    protected final IRoleToPermissionEntityResolver resolver = Jdp.getRequired(IRoleToPermissionEntityResolver.class);
    protected final IRoleToPermissionDTOMapper mapper = Jdp.getRequired(IRoleToPermissionDTOMapper.class);

    @Override
    public ReadAllResponse<RoleToPermissionDTO, FullTrackingWithVersion>
      execute(final RequestContext ctx, final RoleToPermissionSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
