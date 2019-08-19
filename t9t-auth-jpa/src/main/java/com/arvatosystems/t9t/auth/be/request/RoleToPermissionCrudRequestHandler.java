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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.RoleToPermissionDTO;
import com.arvatosystems.t9t.auth.RoleToPermissionInternalKey;
import com.arvatosystems.t9t.auth.RoleToPermissionRef;
import com.arvatosystems.t9t.auth.jpa.entities.RoleToPermissionEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleToPermissionDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleToPermissionEntityResolver;
import com.arvatosystems.t9t.auth.request.RoleToPermissionCrudRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudCompositeRefKey42RequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class RoleToPermissionCrudRequestHandler extends AbstractCrudCompositeRefKey42RequestHandler<
    RoleToPermissionRef, RoleToPermissionInternalKey, RoleToPermissionDTO, FullTrackingWithVersion,
    RoleToPermissionCrudRequest, RoleToPermissionEntity> {

    private final IRoleToPermissionDTOMapper mapper = Jdp.getRequired(IRoleToPermissionDTOMapper.class);

    private final IRoleToPermissionEntityResolver resolver = Jdp.getRequired(IRoleToPermissionEntityResolver.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, RoleToPermissionCrudRequest params) {
        return execute(mapper, resolver, params);
    }
}
