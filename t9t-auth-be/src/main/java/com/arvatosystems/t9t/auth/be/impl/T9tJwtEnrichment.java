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
package com.arvatosystems.t9t.auth.be.impl;

import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment;
import com.arvatosystems.t9t.auth.services.ITenantResolver;
import com.arvatosystems.t9t.auth.services.IUserResolver;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The default enrichment populates desired elements of the z field. */
@Singleton
@Fallback
public class T9tJwtEnrichment implements IJwtEnrichment {

    private static final Logger LOGGER = LoggerFactory.getLogger(T9tJwtEnrichment.class);
    public static final String CSS_SELECTOR_KEY = "cssSelector";

    private final Provider<RequestContext> contextProvider = Jdp.getProvider(RequestContext.class);
    private final IUserResolver userResolver = Jdp.getRequired(IUserResolver.class); // BE resolver from AUTH module!
    private final ITenantResolver tenantResolver = Jdp.getRequired(ITenantResolver.class); // BE resolver from AUTH module!

    @Override
    public void enrichJwt(final JwtInfo jwt, final JwtInfo oldJwt) {
        // don't change any IDs, but look up new refs if the tenant has changed
        if (jwt.getTenantRef().equals(oldJwt.getTenantRef())) {
            // nothing changes
            LOGGER.debug("enrichJwt for identical tenant ID/ref = {}/{}, current tenant {})",
                    jwt.getTenantId(), jwt.getTenantRef(), contextProvider.get().getTenantRef());
            jwt.setZ(oldJwt.getZ());
            return;
        }
        // we know this does not come via API key, because for that, the tenant cannot be changed
        // any restrictions can be user specific only. Read user and tenant (which we probably do not need) and forward to pwd enrichment case
        final UserDTO user = userResolver.getDTO(jwt.getUserRef());
        final TenantDTO tenant = tenantResolver.getDTO(jwt.getTenantRef());
        enrichJwt(jwt, tenant, user);
    }

    @Override
    public void enrichJwt(final JwtInfo jwt, final TenantDTO tenant, final UserDTO user) {
        storeIfNotNull(jwt, CSS_SELECTOR_KEY, tenant.getZ() == null ? null : tenant.getZ().get(CSS_SELECTOR_KEY));
    }
}
