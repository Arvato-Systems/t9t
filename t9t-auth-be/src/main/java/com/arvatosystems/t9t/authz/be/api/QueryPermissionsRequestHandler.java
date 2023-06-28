/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import com.arvatosystems.t9t.authz.api.QueryPermissionsRequest;
import com.arvatosystems.t9t.authz.api.QueryPermissionsResponse;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.dp.Jdp;
import java.util.List;

public class QueryPermissionsRequestHandler extends AbstractReadOnlyRequestHandler<QueryPermissionsRequest> {

    private final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

    @Override
    public QueryPermissionsResponse execute(final RequestContext ctx, final QueryPermissionsRequest request) throws Exception {
        final List<PermissionEntry> permissions = authorizer.getAllPermissions(ctx.internalHeaderParameters.getJwtInfo(), request.getPermissionType());
        final QueryPermissionsResponse resp = new QueryPermissionsResponse();
        resp.setPermissions(permissions);
        return resp;
    }
}
