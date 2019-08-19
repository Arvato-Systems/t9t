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

import com.arvatosystems.t9t.auth.be.impl.AuthResponseUtil
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.authc.api.SwitchTenantRequest
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class SwitchTenantRequestHandler extends AbstractRequestHandler<SwitchTenantRequest> {

    @Inject IAuthPersistenceAccess  authPersistenceAccess
    @Inject AuthResponseUtil        authResponseUtil
    @Inject IJwtEnrichment          jwtEnrichment

    override AuthenticationResponse execute(RequestContext ctx, SwitchTenantRequest rq) {
        if (authResponseUtil.ISSUER_USERID_PASSWORD != ctx.internalHeaderParameters.jwtInfo.issuer) {
            // deny this request unless the token was created via userId / password authentication
            throw new T9tException(T9tException.NOT_AUTHORIZED_WRONG_METHOD, "issuer = " + ctx.internalHeaderParameters.jwtInfo.issuer);
        }
        val jwt       = ctx.internalHeaderParameters.jwtInfo.ret$MutableClone(false, false)
        val tenants   = authPersistenceAccess.getAllTenantsForUser(ctx, ctx.userRef)
        val newTenant = tenants.findFirst[tenantId == rq.tenantId]
        if (newTenant === null)
            throw new T9tException(T9tException.NOT_AUTHORIZED);

        if (jwt.tenantId != rq.tenantId) {
            // not just a refresh but a real switch!
            // an inactive tenant still allows to log in, it just does not accept orders any more. Therefore no check done on that
            jwt.tenantId  = newTenant.tenantId;
            jwt.tenantRef = newTenant.tenantRef
            // TODO: update of role and permission (tenant settings could have restricted the JWTs settings)
        }
        jwt.locale               = ctx.internalHeaderParameters.jwtInfo.locale    // keep existing language
        jwt.zoneinfo             = ctx.internalHeaderParameters.jwtInfo.zoneinfo  // keep existing zoneinfo
        // have to reset any timestamp fields. These will be created new
        jwt.issuedAt             = null
        jwt.expiresAt            = null
        jwt.notBefore            = null

        // compute jwt.z field same way as in AuthenticationRequestHandler
        jwt.z = jwtEnrichment.mergeZs(authPersistenceAccess.getUserZ(jwt.userRef), authPersistenceAccess.getTenantZ(jwt.tenantRef))
        jwtEnrichment.enrichJwt(jwt, ctx.internalHeaderParameters.jwtInfo)

        val authResp             = new AuthenticationResponse
        authResp.jwtInfo         = jwt
        authResp.encodedJwt      = authResponseUtil.authResponseFromJwt(jwt, null, ctx.internalHeaderParameters.jwtInfo)
        authResp.tenantName      = newTenant.name
        authResp.tenantNotUnique = tenants.size > 1
        return authResp
    }
}
