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
package com.arvatosystems.t9t.authc.be.api

import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.authc.api.GetTenantsRequest
import com.arvatosystems.t9t.authc.api.GetTenantsResponse
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class GetTenantsRequestHandler extends AbstractReadOnlyRequestHandler<GetTenantsRequest> {

    @Inject IAuthPersistenceAccess   authPersistenceAccess

    override GetTenantsResponse execute(RequestContext ctx, GetTenantsRequest rq) {
        val it = new GetTenantsResponse
        tenants = authPersistenceAccess.getAllTenantsForUser(ctx, ctx.userRef)
        LOGGER.debug("Got {} tenants", tenants.size)
        return it
    }
}
