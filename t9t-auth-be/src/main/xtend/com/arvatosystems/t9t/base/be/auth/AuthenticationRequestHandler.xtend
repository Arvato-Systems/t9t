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
package com.arvatosystems.t9t.base.be.auth

import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.TenantDTO
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.be.impl.AuthResponseUtil
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.auth.services.IExternalAuthentication
import com.arvatosystems.t9t.auth.services.ITenantResolver
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.auth.PasswordAuthentication
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Inject
import de.jpaw.util.ApplicationException

@AddLogger
class AuthenticationRequestHandler extends AbstractRequestHandler<AuthenticationRequest> {

    @Inject IAuthPersistenceAccess  persistenceAccess
    @Inject AuthResponseUtil        authResponseUtil
    @Inject ITenantResolver         tenantResolver
    @Inject IJwtEnrichment          jwtEnrichment
    @Inject IExternalAuthentication externalAuthentication

    override AuthenticationResponse execute(RequestContext ctx, AuthenticationRequest rq) {
        val tempJwt = ctx.internalHeaderParameters.jwtInfo
        val resp    = auth(
            ctx,
            rq.authenticationParameters,
            rq.sessionParameters?.locale   ?: tempJwt.locale,
            rq.sessionParameters?.zoneinfo ?: tempJwt.zoneinfo
        )      // dispatch and perform authentication
        if (resp === null || resp.returnCode != 0 || resp.jwtInfo === null)
            throw new ApplicationException(T9tException.T9T_ACCESS_DENIED)

        val jwt                        = resp.jwtInfo
        if (rq.sessionParameters !== null) {
            jwt.locale                 = rq.sessionParameters.locale
            jwt.zoneinfo               = rq.sessionParameters.zoneinfo
        }
        resp.mustChangePassword        = (resp.passwordExpires !== null && resp.passwordExpires.isBeforeNow)
        resp.encodedJwt                = authResponseUtil.authResponseFromJwt(jwt, rq.sessionParameters, null)
        resp.numberOfIncorrentAttempts = resp.numberOfIncorrentAttempts
        resp.tenantNotUnique           = resp.jwtInfo.tenantId == T9tConstants.GLOBAL_TENANT_ID // only then the user has access to additional ones
        LOGGER.debug("User {} successfully logged in for tenant {}", resp.jwtInfo.userId, resp.jwtInfo.tenantId)
        return resp
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

    def protected JwtInfo createJwt(UserDTO user, TenantDTO tenantDTO) {
        val p = user.permissions
        return new JwtInfo => [
            issuer              = authResponseUtil.ISSUER_USERID_PASSWORD
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

    def protected JwtInfo createJwt(ApiKeyDTO apiKey, TenantDTO tenantDTO, UserDTO userDTO) {
        val p = apiKey.permissions
        val user = apiKey.userRef as UserDTO
        val pu = user.permissions
        return new JwtInfo => [
            issuer              = authResponseUtil.ISSUER_APIKEY
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

    def protected boolean isUserAllowedToLogOn(RequestContext ctx, UserDTO userDto) {
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

    def protected boolean isApiKeyAllowed(RequestContext ctx, ApiKeyDTO apiKey) {
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

    /** Authenticates a user via username / password. Relevant information for the JWT is taken from the UserDTO, then the TenantDTO. */
    def protected dispatch AuthenticationResponse auth(RequestContext ctx, PasswordAuthentication pw, String locale, String zoneinfo) {
        // check for external authentication first
        val ldapConfiguration = ConfigProvider.configuration.ldapConfiguration
        val authResult = if (ldapConfiguration === null) {
            // internal authentication
            persistenceAccess.getByUserIdAndPassword(ctx.executionStart, pw.userId, pw.password, pw.newPassword)
        } else {
            // always external authentication (pass UserDTO as null)
            val user = persistenceAccess.getUserById(pw.userId);
            if (user === null) {
                LOGGER.debug("No master data record for userId {} - declining access", pw.userId)
                return null  // throw new T9tException(T9tException.USER_NOT_FOUND);
            }
            if (Boolean.TRUE != ldapConfiguration.onlySelectedUsers || Boolean.TRUE == user.value.externalAuth) {
                // all users authenticated by external auth, or external auth configured for this user
                externalAuthentication.externalAuth(ctx, pw, user)
            } else {
                // internal authentication for this user
                persistenceAccess.getByUserIdAndPassword(ctx.executionStart, pw.userId, pw.password, pw.newPassword)
            }
        }
        if (authResult === null || (authResult.returnCode != 0 && authResult.returnCode != T9tException.PASSWORD_EXPIRED)) {
            LOGGER.debug("Incorrect authentication for userId {}", pw.userId)
            return null
        }
        val userDto                 = authResult.user
        if (!isUserAllowedToLogOn(ctx, userDto))
            return null

        val tenantDto               = tenantResolver.getDTO(authResult.tenantRef)
        val resp                    = new AuthenticationResponse
        resp.jwtInfo                = createJwt(userDto, tenantDto)
        resp.jwtInfo.locale         = locale
        resp.jwtInfo.zoneinfo       = zoneinfo
        jwtEnrichment.enrichJwt(resp.jwtInfo, tenantDto, userDto)

        resp.tenantName             = tenantDto?.name
        resp.jwtInfo.name           = userDto.name
        resp.passwordExpires        = authResult.authExpires
        if (authResult.userStatus !== null) {
            resp.lastLoginUser      = authResult.userStatus.prevLogin
            resp.lastLoginMethod    = authResult.userStatus.prevLoginByPassword
            resp.numberOfIncorrentAttempts = authResult.userStatus.numberOfIncorrectAttempts
        }
        resp.returnCode             = authResult.returnCode
        return resp
    }

    /** Authenticates a user via API key. Relevant information for the JWT is taken from the ApiKeyDTO, then the UserDTO, finally the TenantDTO. */
    def protected dispatch AuthenticationResponse auth(RequestContext ctx, ApiKeyAuthentication ap, String locale, String zoneinfo) {
        val authResult = persistenceAccess.getByApiKey(ctx.executionStart, ap.apiKey)
        if (authResult === null || authResult.returnCode != 0) {
            LOGGER.debug("Incorrect authentication for API key {}", ap.apiKey)
            return null
        }
        val tenantDto   = tenantResolver.getDTO(authResult.tenantRef)
        if (tenantDto === null)
            return null

        val apiKeyDto               = authResult.apiKey
        if (!isApiKeyAllowed(ctx, apiKeyDto))
            return null

        val userDto                 = apiKeyDto.userRef as UserDTO
        if (!isUserAllowedToLogOn(ctx, userDto))
            return null

        val resp                    = new AuthenticationResponse
        resp.jwtInfo                = createJwt(apiKeyDto, tenantDto, userDto)
        resp.jwtInfo.locale         = locale
        resp.jwtInfo.zoneinfo       = zoneinfo
        jwtEnrichment.enrichJwt(resp.jwtInfo, tenantDto, userDto, apiKeyDto)

        resp.tenantName             = tenantDto.name
        resp.passwordExpires        = authResult.authExpires
        if (authResult.userStatus !== null) {
            resp.lastLoginUser      = authResult.userStatus.prevLogin
            resp.lastLoginMethod    = authResult.userStatus.prevLoginByApiKey
        }
        return resp
    }

    def protected dispatch auth(RequestContext ctx, AuthenticationParameters unknown, String locale, String zoneinfo) {
        throw new UnsupportedOperationException("Unsupported authentication parameters")
    }
}
