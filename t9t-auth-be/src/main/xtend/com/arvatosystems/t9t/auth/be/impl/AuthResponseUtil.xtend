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
package com.arvatosystems.t9t.auth.be.impl

import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.SessionDTO
import com.arvatosystems.t9t.auth.TenantDTO
import com.arvatosystems.t9t.auth.TenantRef
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserRef
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.auth.services.IAuthResponseUtil
import com.arvatosystems.t9t.auth.services.IAuthenticator
import com.arvatosystems.t9t.base.services.IRefGenerator
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.base.types.SessionParameters
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import de.jpaw.bonaparte.util.ToStringHelper
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.UUID

@AddLogger
@Singleton
class AuthResponseUtil implements IAuthResponseUtil {
    public final String ISSUER_APIKEY          = "AK";
    public final String ISSUER_USERID_PASSWORD = "UP";
    public final Long   DEFAULT_JWT_VALIDITY   = 12L * 60 * 60

    @Inject IAuthenticator          authenticator
    @Inject IAuthPersistenceAccess  persistenceAccess
    @Inject IJwtEnrichment          jwtEnrichment
    @Inject IRefGenerator           refGenerator

    final Long jwtValidityApiKey;
    final Long jwtValidityUserPassword;
    final boolean sessionLogSysout

    new() {
        val serverCfg = ConfigProvider.getConfiguration;
        jwtValidityApiKey       = serverCfg.jwtValidityApiKey       ?: DEFAULT_JWT_VALIDITY
        jwtValidityUserPassword = serverCfg.jwtValidityUserPassword ?: DEFAULT_JWT_VALIDITY
        sessionLogSysout        = Boolean.TRUE == serverCfg.sessionLogSysout
    }

    override boolean isUserAllowedToLogOn(RequestContext ctx, UserDTO userDto) {
        if (!userDto.isActive) {
            LOGGER.debug("Authentication of userId {} denied, user is inactive", userDto.userId)
            return false
        }
        if (userDto.permissions !== null) {
            if (userDto.permissions.validFrom !== null && userDto.permissions.validFrom.isAfter(ctx.executionStart)) {
                LOGGER.debug("Authentication of userId {} denied, user is not allowed before {}", userDto.userId, userDto.permissions.validFrom)
                return false
            }
            if (userDto.permissions.validTo !== null && userDto.permissions.validTo.isBefore(ctx.executionStart)) {
                LOGGER.debug("Authentication of userId {} denied, user is not allowed after {}", userDto.userId, userDto.permissions.validTo)
                return false
            }
        }
        return true  // all tests passed
    }

    override boolean isApiKeyAllowed(RequestContext ctx, ApiKeyDTO apiKey) {
        if (!apiKey.isActive) {
            LOGGER.debug("Authentication via API key {} denied, key is inactive", apiKey.apiKey)
            return false
        }
        if (apiKey.permissions !== null) {
            if (apiKey.permissions.validFrom !== null && apiKey.permissions.validFrom.isAfter(ctx.executionStart)) {
                LOGGER.debug("Authentication via API key {} denied, key is not allowed before {}", apiKey.apiKey, apiKey.permissions.validFrom)
                return false
            }
            if (apiKey.permissions.validTo !== null && apiKey.permissions.validTo.isBefore(ctx.executionStart)) {
                LOGGER.debug("Authentication via API key {} denied, key is not allowed after {}", apiKey.apiKey, apiKey.permissions.validTo)
                return false
            }
        }
        return true  // all tests passed
    }

    def String normalize(String in) {
        if (in === null)
            return null
        val trimmed = in.trim
        if (trimmed.length == 0)
            return null
        return trimmed
    }

    // common part of code for SwitchTenantRequest and AuthenticationRequest
    override String authResponseFromJwt(JwtInfo jwt, SessionParameters sp, JwtInfo continuesFromJwt) {
        jwt.sessionId           = UUID.randomUUID
        jwt.sessionRef          = refGenerator.generateRef(SessionDTO.class$rtti)
        jwt.resource            = jwt.resource.normalize  // strip any leading or trailing spaces, and convert to null in case those were the only content
        // create a session
        val session = new SessionDTO => [
            sessionId           = jwt.sessionId
            objectRef           = jwt.sessionRef
            locale              = jwt.locale
            zoneinfo            = jwt.zoneinfo
            userRef             = new UserRef(jwt.userRef)
            tenantRef           = new TenantRef(jwt.tenantRef)
            if (sp !== null) {
                // memorize session parameters
                locale          = sp.locale
                userAgent       = sp.userAgent
                dataUri         = sp.dataUri
                zoneinfo        = sp.zoneinfo
            }
            if (continuesFromJwt !== null) {
                // transition reference to prior session
                continuesFromSession = continuesFromJwt.sessionRef
            }
        ]

        if (UserLogLevelType.STEALTH != jwt.logLevel) {
            // only if we do not want ANY info (such as from ping requests)
            if (sessionLogSysout) {
                // do not store session info to DB
                LOGGER.info("New session created: tenant {}, user {}, sessionRef {}", jwt.tenantId, jwt.userId, jwt.sessionRef)
            } else {
                persistenceAccess.storeSession(session)
            }
        }

        LOGGER.debug("About to sign the following Jwt: {}", ToStringHelper.toStringML(jwt))
        val duration = if (ISSUER_APIKEY == jwt.issuer) jwtValidityApiKey else if (ISSUER_USERID_PASSWORD == jwt.issuer) jwtValidityUserPassword else DEFAULT_JWT_VALIDITY
        return authenticator.doSign(jwt, duration)
    }

    def protected void enrichJwtWithTenantDefaults(JwtInfo it, PermissionsDTO pt) {
        if (pt !== null) {
            if (permissionsMin === null)
                permissionsMin = pt.minPermissions
            if (permissionsMax === null)
                permissionsMax = pt.maxPermissions
            if (logLevel === null)
                logLevel = pt.logLevel
            if (logLevelErrors === null)
                logLevelErrors = pt.logLevelErrors
            // fallbacks for resource do not make sense
        }
        // check if min / max permissions are still null, then set to defaults...  (not required, those are assumed at evaluation time...)
//        if (permissionsMin === null)
//            permissionsMin = new Permissionset(0)
//        if (permissionsMax === null)
//            permissionsMax = new Permissionset(0xfffff)
    }

    override JwtInfo createJwt(UserDTO user, TenantDTO tenantDTO) {
        val p = user.permissions
        return new JwtInfo => [
            issuer              = ISSUER_USERID_PASSWORD
            roleRef             = user.roleRef?.objectRef
            // same from here
            userRef             = user.objectRef
            userId              = user.userId
            name                = user.name
            tenantId            = tenantDTO.tenantId
            tenantRef           = tenantDTO.objectRef
            roleRef             = user.roleRef?.objectRef
            z                   = jwtEnrichment.mergeZs(user.z, tenantDTO.z)
            if (p !== null) {
                permissionsMin      = p.minPermissions
                permissionsMax      = p.maxPermissions
                logLevel            = p.logLevel
                logLevelErrors      = p.logLevelErrors
                resource            = p.resourceRestriction
                resourceIsWildcard  = p.resourceIsWildcard
            }
            enrichJwtWithTenantDefaults(tenantDTO.permissions)
        ]
    }

    override JwtInfo createJwt(ApiKeyDTO apiKey, TenantDTO tenantDTO, UserDTO userDTO) {
        val p = apiKey.permissions
        val user = apiKey.userRef as UserDTO
        val pu = user.permissions
        return new JwtInfo => [
            issuer              = ISSUER_APIKEY
            roleRef             = apiKey.roleRef?.objectRef
            // same from here
            userRef             = user.objectRef
            userId              = user.userId
            name                = user.name
            tenantId            = tenantDTO.tenantId
            tenantRef           = tenantDTO.objectRef
            roleRef             = apiKey.roleRef?.objectRef ?: user.roleRef?.objectRef
            z                   = jwtEnrichment.mergeZs(user.z, tenantDTO.z)
            if (p !== null) {
                permissionsMin      = p.minPermissions ?: pu?.minPermissions
                permissionsMax      = p.maxPermissions ?: pu?.maxPermissions
                logLevel            = p.logLevel
                logLevelErrors      = p.logLevelErrors
                resource            = p.resourceRestriction
                resourceIsWildcard  = p.resourceIsWildcard
            }
            enrichJwtWithTenantDefaults(tenantDTO.permissions)
        ]
    }
}
