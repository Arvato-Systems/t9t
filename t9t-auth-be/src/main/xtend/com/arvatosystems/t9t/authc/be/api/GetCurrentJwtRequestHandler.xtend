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

import com.arvatosystems.t9t.auth.services.ITenantResolver
import com.arvatosystems.t9t.authc.api.GetCurrentJwtRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class GetCurrentJwtRequestHandler extends AbstractReadOnlyRequestHandler<GetCurrentJwtRequest> {

    @Inject ITenantResolver        tenantResolver

    override AuthenticationResponse execute(RequestContext ctx, GetCurrentJwtRequest rq) {
        val tenantDTO            = tenantResolver.getDTO(ctx.tenantRef)

        val authResp             = new AuthenticationResponse
        authResp.jwtInfo         = ctx.internalHeaderParameters.jwtInfo
        authResp.encodedJwt      = ctx.internalHeaderParameters.encodedJwt
        authResp.tenantName      = tenantDTO.name
        return authResp
    }
}
