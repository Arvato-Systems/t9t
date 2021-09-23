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

import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment
import com.arvatosystems.t9t.auth.hooks.IOtherAuthentication
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.auth.services.IAuthResponseUtil
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
import de.jpaw.dp.Inject
import de.jpaw.util.ApplicationException
import java.time.Instant

@AddLogger
class AuthenticationRequestHandler extends AbstractRequestHandler<AuthenticationRequest> {

    @Inject IAuthPersistenceAccess  persistenceAccess
    @Inject IAuthResponseUtil       authResponseUtil
    @Inject ITenantResolver         tenantResolver
    @Inject IJwtEnrichment          jwtEnrichment
    @Inject IExternalAuthentication externalAuthentication
    @Inject IOtherAuthentication    otherAuthentication

    override AuthenticationResponse execute(RequestContext ctx, AuthenticationRequest rq) {
        val tempJwt = ctx.internalHeaderParameters.jwtInfo
        val ap = otherAuthentication.preprocess(ctx, rq.sessionParameters, rq.authenticationParameters)
        val resp    = auth(
            ctx,
            ap,
            rq.sessionParameters?.locale   ?: tempJwt.locale,
            rq.sessionParameters?.zoneinfo ?: tempJwt.zoneinfo
        )      // dispatch and perform authentication
        if (resp === null || (!ApplicationException.isOk(resp.returnCode) && resp.returnCode != T9tException.PASSWORD_EXPIRED) || resp.jwtInfo === null)
            throw new ApplicationException(T9tException.T9T_ACCESS_DENIED)

        val jwt                        = resp.jwtInfo
        if (rq.sessionParameters !== null) {
            jwt.locale                 = rq.sessionParameters.locale
            jwt.zoneinfo               = rq.sessionParameters.zoneinfo
        }
        resp.mustChangePassword        = (resp.passwordExpires !== null && resp.passwordExpires.isBefore(Instant.now))
        resp.encodedJwt                = authResponseUtil.authResponseFromJwt(jwt, rq.sessionParameters, null)
        resp.numberOfIncorrectAttempts = resp.numberOfIncorrectAttempts
        resp.tenantNotUnique           = resp.jwtInfo.tenantId == T9tConstants.GLOBAL_TENANT_ID // only then the user has access to additional ones
        LOGGER.debug("User {} successfully logged in for tenant {}", resp.jwtInfo.userId, resp.jwtInfo.tenantId)
        return resp
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
        if (authResult === null || (!ApplicationException.isOk(authResult.returnCode) && authResult.returnCode != T9tException.PASSWORD_EXPIRED)) {
            LOGGER.debug("Incorrect authentication for userId {}", pw.userId)
            return null
        }
        val userDto                 = authResult.user
        if (!authResponseUtil.isUserAllowedToLogOn(ctx, userDto))
            return null

        val tenantDto               = tenantResolver.getDTO(authResult.tenantRef)
        val resp                    = new AuthenticationResponse
        resp.jwtInfo                = authResponseUtil.createJwt(userDto, tenantDto)
        resp.jwtInfo.locale         = locale
        resp.jwtInfo.zoneinfo       = zoneinfo
        jwtEnrichment.enrichJwt(resp.jwtInfo, tenantDto, userDto)

        resp.tenantName             = tenantDto?.name
        resp.jwtInfo.name           = userDto.name
        resp.passwordExpires        = authResult.authExpires
        if (authResult.userStatus !== null) {
            resp.lastLoginUser      = authResult.userStatus.prevLogin
            resp.lastLoginMethod    = authResult.userStatus.prevLoginByPassword
            resp.numberOfIncorrectAttempts = authResult.userStatus.numberOfIncorrectAttempts
        }
        resp.returnCode             = authResult.returnCode
        return resp
    }

    /** Authenticates a user via API key. Relevant information for the JWT is taken from the ApiKeyDTO, then the UserDTO, finally the TenantDTO. */
    def protected dispatch AuthenticationResponse auth(RequestContext ctx, ApiKeyAuthentication ap, String locale, String zoneinfo) {
        val authResult = persistenceAccess.getByApiKey(ctx.executionStart, ap.apiKey)
        if (authResult === null || (!ApplicationException.isOk(authResult.returnCode) && authResult.returnCode != T9tException.PASSWORD_EXPIRED)) {
            LOGGER.debug("Incorrect authentication for API key {}", ap.apiKey)
            return null
        }
        val tenantDto   = tenantResolver.getDTO(authResult.tenantRef)
        if (tenantDto === null)
            return null

        val apiKeyDto               = authResult.apiKey
        if (!authResponseUtil.isApiKeyAllowed(ctx, apiKeyDto))
            return null

        val userDto                 = apiKeyDto.userRef as UserDTO
        if (!authResponseUtil.isUserAllowedToLogOn(ctx, userDto))
            return null

        val resp                    = new AuthenticationResponse
        resp.jwtInfo                = authResponseUtil.createJwt(apiKeyDto, tenantDto, userDto)
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
        return otherAuthentication.auth(ctx, unknown, locale, zoneinfo)
    }
}
