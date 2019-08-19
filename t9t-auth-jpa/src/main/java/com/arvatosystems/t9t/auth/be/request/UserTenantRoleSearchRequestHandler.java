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

import com.arvatosystems.t9t.auth.UserTenantRoleDTO;
import com.arvatosystems.t9t.auth.jpa.mapping.IUserTenantRoleDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserTenantRoleEntityResolver;
import com.arvatosystems.t9t.auth.request.UserTenantRoleSearchRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.dp.Jdp;

/** This search request has to add conditions on the tenant of the child entities of user and role: none of the returned objects may refer to an entity of a different non-@ tenant. */
public class UserTenantRoleSearchRequestHandler extends AbstractSearchRequestHandler<UserTenantRoleSearchRequest> {
    protected final IUserTenantRoleEntityResolver resolver = Jdp.getRequired(IUserTenantRoleEntityResolver.class);
    protected final IUserTenantRoleDTOMapper mapper = Jdp.getRequired(IUserTenantRoleDTOMapper.class);

    @Override
    public ReadAllResponse<UserTenantRoleDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final UserTenantRoleSearchRequest request) throws Exception {
        mapper.processSearchPrefixForDB(request); // convert the field with searchPrefix

        final LongFilter userFilter = ctx.tenantFilter("user.tenantRef");
        final LongFilter roleFilter = ctx.tenantFilter("role.tenantRef");
        request.setSearchFilter(SearchFilters.and(request.getSearchFilter(), SearchFilters.and(userFilter, roleFilter)));
        return mapper.createReadAllResponse(resolver.search(request, null), request.getSearchOutputTarget());
    }
}
