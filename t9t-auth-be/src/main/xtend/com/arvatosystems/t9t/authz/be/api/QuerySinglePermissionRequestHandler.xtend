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
package com.arvatosystems.t9t.authz.be.api

import com.arvatosystems.t9t.authz.api.QuerySinglePermissionRequest
import com.arvatosystems.t9t.authz.api.QuerySinglePermissionResponse
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.server.services.IAuthorize
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class QuerySinglePermissionRequestHandler extends AbstractReadOnlyRequestHandler<QuerySinglePermissionRequest> {

    @Inject IAuthorize authorizer

    override QuerySinglePermissionResponse execute(RequestContext ctx, QuerySinglePermissionRequest rq) {
        return new QuerySinglePermissionResponse => [
            permissions = authorizer.getPermissions(ctx.internalHeaderParameters.jwtInfo, rq.permissionType, rq.resourceId)
        ]
    }
}
