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
package com.arvatosystems.t9t.auth.hooks;

import java.util.HashMap;
import java.util.Map;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

/** Hook for enrichment of a Jwt during logins.
 * A prepopulated modifiable JWT is passed, just before it is signed (session ID / Ref and expiration timestamps will still be missing).
 *
 * An implementation can modify fields as required.
 * It is recommend to only add the z field of the Jwt.
 * Data put into the JWT should be small, because it is transferred with every request.
 *
 * The methods are called while we are in a valid transaction context.
 * There are separate methods per authentication type.
 *
 * The default implementation does nothing except storing some CSS selector.
 *
 * Some OMS default customization for example will in some cases store the following fields in the z field of the Jwt:
 * - oi (String) orgUnitId if the user is restricted to a single orgUnit, "*" if there is no restriction. In other cases, it is left null.
 * - or (Long)   orgUnitRef if the user is restricted to a single orgUnit
 * - od (String) the orgUnitId of a default selection (in case more than one is possible, but one is the preferred one)
 * - li (String) locationId if the user is restricted to a single location (drop shipper or retail store), "*" if there is no restriction.
 *               In other cases, it is left null.
 * - lr (Long)   locationRef if the user is restricted to a single location
 * - ld (String) the locationId of a default selection (in case more than one is possible, but one is the preferred one)
 *
 */
public interface IJwtEnrichment {
    /** Called to enrich the Jwt after a password authentication. */
    void enrichJwt(JwtInfo jwt, TenantDTO tenant, UserDTO user);

    /** Called to enrich the Jwt after authentication via API key. */
    default void enrichJwt(JwtInfo jwt, TenantDTO tenant, UserDTO user, ApiKeyDTO apiKey) { }

    /**
     * Called to enrich the Jwt after a SwitchTenantRequest (which can also be used to refresh the Jwt or to switch languages,
     * check if old and new tenant are identical). */
    default void enrichJwt(JwtInfo jwt, JwtInfo oldJwt) { }

    default void storeIfNotNull(JwtInfo jwt, String key, Object value) {
        if (value == null) {
            return; // nothing to do
        }
        if (jwt.getZ() == null) {
            jwt.setZ(new HashMap<>());
        }
        jwt.getZ().put(key, value);
    }

    default Map<String, Object> mergeZs(Map<String, Object> userVal, Map<String, Object> tenantVal) {
        if (userVal == null)
            return tenantVal;
        if (tenantVal == null)
            return userVal;
        // both are populated, merge them
        final Map<String, Object> result = new HashMap<String, Object>(tenantVal);
        result.putAll(userVal);
        return result;
    }
}
