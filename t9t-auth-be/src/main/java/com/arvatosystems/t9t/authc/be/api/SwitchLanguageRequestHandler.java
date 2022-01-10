/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.authc.be.api;

import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.auth.services.IAuthResponseUtil;
import com.arvatosystems.t9t.authc.api.SwitchLanguageRequest;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import java.util.List;

public class SwitchLanguageRequestHandler extends AbstractRequestHandler<SwitchLanguageRequest> {

    private final IAuthPersistenceAccess authPersistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);
    private final IAuthResponseUtil authResponseUtil = Jdp.getRequired(IAuthResponseUtil.class);
    private final IJwtEnrichment jwtEnrichment = Jdp.getRequired(IJwtEnrichment.class);

    @Override
    public AuthenticationResponse execute(final RequestContext ctx, final SwitchLanguageRequest request) throws Exception {
        final JwtInfo jwt = ctx.internalHeaderParameters.getJwtInfo().ret$MutableClone(false, false);
        final List<TenantDescription> tenants = authPersistenceAccess.getAllTenantsForUser(ctx, ctx.userRef);
        TenantDescription newTenant = null;

        for (TenantDescription t : tenants) {
            if (t.getTenantId().equals(ctx.tenantId)) {
                newTenant = t;
                break;
            }
        }

        if (newTenant == null) {
            throw new T9tException(T9tException.NOT_AUTHORIZED); // permission was revoked
        }

        jwt.setLocale(request.getLanguage()); // transfer new language into JWT
        jwt.setZoneinfo(ctx.internalHeaderParameters.getJwtInfo().getZoneinfo()); // keep existing zoneinfo

        // have to reset any timestamp fields. These will be created new
        jwt.setIssuedAt(null);
        jwt.setExpiresAt(null);
        jwt.setNotBefore(null);
        jwtEnrichment.enrichJwt(jwt, ctx.internalHeaderParameters.getJwtInfo());

        final AuthenticationResponse authResp = new AuthenticationResponse();
        authResp.setJwtInfo(jwt);
        authResp.setEncodedJwt(authResponseUtil.authResponseFromJwt(jwt, null, ctx.internalHeaderParameters.getJwtInfo()));
        authResp.setTenantName(newTenant.getName());
        authResp.setTenantNotUnique(tenants.size() > 1);
        return authResp;
    }
}
