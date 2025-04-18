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

import com.arvatosystems.t9t.auth.UserTenantRoleDTO;
import com.arvatosystems.t9t.auth.UserTenantRoleInternalKey;
import com.arvatosystems.t9t.auth.UserTenantRoleRef;
import com.arvatosystems.t9t.auth.jpa.entities.UserTenantRoleEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IUserTenantRoleDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserTenantRoleEntityResolver;
import com.arvatosystems.t9t.auth.request.UserTenantRoleCrudRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudCompositeRefKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class UserTenantRoleCrudRequestHandler extends AbstractCrudCompositeRefKeyRequestHandler<
    UserTenantRoleRef, UserTenantRoleInternalKey, UserTenantRoleDTO, FullTrackingWithVersion,
    UserTenantRoleCrudRequest, UserTenantRoleEntity> {

    private final IUserTenantRoleDTOMapper mapper = Jdp.getRequired(IUserTenantRoleDTOMapper.class);

    private final IUserTenantRoleEntityResolver resolver = Jdp.getRequired(IUserTenantRoleEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final UserTenantRoleCrudRequest params) {
        return execute(ctx, mapper, resolver, params);
    }
}
