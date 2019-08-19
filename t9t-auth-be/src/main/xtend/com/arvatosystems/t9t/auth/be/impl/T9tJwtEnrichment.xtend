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
package com.arvatosystems.t9t.auth.be.impl

import com.arvatosystems.t9t.auth.TenantDTO
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment
import com.arvatosystems.t9t.auth.services.ITenantResolver
import com.arvatosystems.t9t.auth.services.IUserResolver
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Fallback
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import de.jpaw.dp.Singleton

/** The default enrichment populates desired elements of the z field. */
@AddLogger
@Singleton
@Fallback
class T9tJwtEnrichment implements IJwtEnrichment {
    public static final String CSS_SELECTOR_KEY = "cssSelector";
    @Inject Provider<RequestContext>    contextProvider
    @Inject IUserResolver               userResolver    // BE resolver from AUTH module!
    @Inject ITenantResolver             tenantResolver  // BE resolver from AUTH module!

    override enrichJwt(JwtInfo jwt, JwtInfo oldJwt) {
        // don't change any IDs, but look up new refs if the tenant has changed
        if (jwt.tenantRef == oldJwt.tenantRef) {
            // nothing changes
            LOGGER.debug("enrichJwt for identical tenant ID/ref = {}/{}, current tenant {})", jwt.tenantId, jwt.tenantRef, contextProvider.get.tenantRef)
            jwt.z = oldJwt.z
            return
        }
        // we know this does not come via API key, because for that, the tenant cannot be changed
        // any restrictions can be user specific only. Read user and tenant (which we probably do not need) and forward to pwd enrichment case
        val user = userResolver.getDTO(jwt.userRef)
        val tenant = tenantResolver.getDTO(jwt.tenantRef)
        enrichJwt(jwt, tenant, user)
    }

    override enrichJwt(JwtInfo jwt, TenantDTO tenant, UserDTO user) {
        storeIfNotNull(jwt, CSS_SELECTOR_KEY, tenant.z?.get(CSS_SELECTOR_KEY))
    }
}
