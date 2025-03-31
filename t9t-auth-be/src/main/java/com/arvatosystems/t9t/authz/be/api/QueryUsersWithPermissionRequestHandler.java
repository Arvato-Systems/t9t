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
package com.arvatosystems.t9t.authz.be.api;

import java.util.List;

import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.authz.api.QueryUsersWithPermissionRequest;
import com.arvatosystems.t9t.authz.api.QueryUsersWithPermissionResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class QueryUsersWithPermissionRequestHandler extends AbstractReadOnlyRequestHandler<QueryUsersWithPermissionRequest> {

    private final IAuthPersistenceAccess authPersistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);

    @Override
    public QueryUsersWithPermissionResponse execute(final RequestContext ctx, final QueryUsersWithPermissionRequest request) throws Exception {

        final List<UserData> usersWithPermission = authPersistenceAccess.getUsersWithPermission(ctx.internalHeaderParameters.getJwtInfo(),
                request.getPermissionType(), request.getResourceId(), request.getOperationTypes());

        final QueryUsersWithPermissionResponse resp = new QueryUsersWithPermissionResponse();
        resp.setUsers(usersWithPermission);

        return resp;
    }
}
