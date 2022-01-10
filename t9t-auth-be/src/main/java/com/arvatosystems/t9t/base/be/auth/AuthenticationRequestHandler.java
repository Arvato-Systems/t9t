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
package com.arvatosystems.t9t.base.be.auth;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment;
import com.arvatosystems.t9t.auth.hooks.IOtherAuthentication;
import com.arvatosystems.t9t.auth.services.AuthIntermediateResult;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.auth.services.IAuthResponseUtil;
import com.arvatosystems.t9t.auth.services.IExternalAuthentication;
import com.arvatosystems.t9t.auth.services.ITenantResolver;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.LdapConfiguration;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import java.time.Instant;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationRequestHandler extends AbstractRequestHandler<AuthenticationRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationRequestHandler.class);
    private final IAuthPersistenceAccess persistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);
    private final IAuthResponseUtil authResponseUtil = Jdp.getRequired(IAuthResponseUtil.class);
    private final ITenantResolver tenantResolver = Jdp.getRequired(ITenantResolver.class);
    private final IJwtEnrichment jwtEnrichment = Jdp.getRequired(IJwtEnrichment.class);
    private final IExternalAuthentication externalAuthentication = Jdp.getRequired(IExternalAuthentication.class);
    private final IOtherAuthentication otherAuthentication = Jdp.getRequired(IOtherAuthentication.class);

    @Override
    public AuthenticationResponse execute(final RequestContext ctx, final AuthenticationRequest rq) throws Exception {
        final JwtInfo tempJwt = ctx.internalHeaderParameters.getJwtInfo();
        final AuthenticationParameters ap = otherAuthentication.preprocess(ctx, rq.getSessionParameters(), rq.getAuthenticationParameters());
        final String locale = (rq.getSessionParameters() == null ? null : rq.getSessionParameters().getLocale()) == null
                ? tempJwt.getLocale() : rq.getSessionParameters().getLocale();
        final String zoneinfo = (rq.getSessionParameters() == null ? null : rq.getSessionParameters().getZoneinfo()) == null
                ? tempJwt.getZoneinfo() : rq.getSessionParameters().getZoneinfo();
        final AuthenticationResponse resp = auth(ctx, ap, locale, zoneinfo); // dispatch and perform authentication
        if (resp == null
          || (!ApplicationException.isOk(resp.getReturnCode()) && resp.getReturnCode() != T9tException.PASSWORD_EXPIRED)
          || resp.getJwtInfo() == null)
            throw new ApplicationException(T9tException.T9T_ACCESS_DENIED);

        final JwtInfo jwt = resp.getJwtInfo();
        if (rq.getSessionParameters() != null) {
            jwt.setLocale(rq.getSessionParameters().getLocale());
            jwt.setZoneinfo(rq.getSessionParameters().getZoneinfo());
        }
        resp.setMustChangePassword(resp.getPasswordExpires() != null && resp.getPasswordExpires().isBefore(Instant.now()));
        resp.setEncodedJwt(authResponseUtil.authResponseFromJwt(jwt, rq.getSessionParameters(), null));
        resp.setNumberOfIncorrectAttempts(resp.getNumberOfIncorrectAttempts());
        resp.setTenantNotUnique(resp.getJwtInfo().getTenantId().equals(T9tConstants.GLOBAL_TENANT_ID)); // only then the user has access to additional ones
        LOGGER.debug("User {} successfully logged in for tenant {}", resp.getJwtInfo().getUserId(), resp.getJwtInfo().getTenantId());
        return resp;
    }

    /** Authenticates a user via username / password. Relevant information for the JWT is taken from the UserDTO, then the TenantDTO. */
    protected AuthenticationResponse authPasswordAuthentication(final RequestContext ctx,
            final PasswordAuthentication pw, final String locale, final String zoneinfo) {
        // check for external authentication first
        final LdapConfiguration ldapConfiguration = ConfigProvider.getConfiguration().getLdapConfiguration();
        AuthIntermediateResult authResult = null;

        if (ldapConfiguration == null) {
            // internal authentication
            authResult = persistenceAccess.getByUserIdAndPassword(ctx.executionStart, pw.getUserId(), pw.getPassword(), pw.getNewPassword());
        } else {
            // always external authentication (pass UserDTO as null)
            final DataWithTrackingW<UserDTO, FullTrackingWithVersion> user = persistenceAccess.getUserById(pw.getUserId());
            if (user == null) {
                LOGGER.debug("No master data record for userId {} - declining access", pw.getUserId());
                return null; // throw new T9tException(T9tException.USER_NOT_FOUND);
            }
            if (!Boolean.TRUE.equals(ldapConfiguration.getOnlySelectedUsers()) || Boolean.TRUE.equals(user.getData().getExternalAuth())) {
                // all users authenticated by external auth, or external auth configured for this user
                authResult = externalAuthentication.externalAuth(ctx, pw, user);
            } else {
                // internal authentication for this user
                authResult = persistenceAccess.getByUserIdAndPassword(ctx.executionStart, pw.getUserId(), pw.getPassword(), pw.getNewPassword());
            }
        }
        if (authResult == null || (!ApplicationException.isOk(authResult.getReturnCode()) && authResult.getReturnCode() != T9tException.PASSWORD_EXPIRED)) {
            LOGGER.debug("Incorrect authentication for userId {}", pw.getUserId());
            return null;
        }
        final UserDTO userDto = authResult.getUser();
        if (!authResponseUtil.isUserAllowedToLogOn(ctx, userDto))
            return null;

        final TenantDTO tenantDto = tenantResolver.getDTO(authResult.getTenantRef());
        final AuthenticationResponse resp = new AuthenticationResponse();
        resp.setJwtInfo(authResponseUtil.createJwt(userDto, tenantDto));
        resp.getJwtInfo().setLocale(locale);
        resp.getJwtInfo().setZoneinfo(zoneinfo);
        jwtEnrichment.enrichJwt(resp.getJwtInfo(), tenantDto, userDto);

        resp.setTenantName(tenantDto == null ? null : tenantDto.getName());
        resp.getJwtInfo().setName(userDto.getName());
        resp.setPasswordExpires(authResult.getAuthExpires());
        if (authResult.getUserStatus() != null) {
            resp.setLastLoginUser(authResult.getUserStatus().getPrevLogin());
            resp.setLastLoginMethod(authResult.getUserStatus().getPrevLoginByPassword());
            resp.setNumberOfIncorrectAttempts(authResult.getUserStatus().getNumberOfIncorrectAttempts());
        }
        resp.setReturnCode(authResult.getReturnCode());
        return resp;
    }

    /** Authenticates a user via API key. Relevant information for the JWT is taken from the ApiKeyDTO, then the UserDTO, finally the TenantDTO. */
    protected AuthenticationResponse authApiKeyAuthentication(final RequestContext ctx, final ApiKeyAuthentication ap,
            final String locale, final String zoneinfo) {
        final AuthIntermediateResult authResult = persistenceAccess.getByApiKey(ctx.executionStart, ap.getApiKey());
        if (authResult == null || (!ApplicationException.isOk(authResult.getReturnCode()) && authResult.getReturnCode() != T9tException.PASSWORD_EXPIRED)) {
            LOGGER.debug("Incorrect authentication for API key {}", ap.getApiKey());
            return null;
        }
        final TenantDTO tenantDto   = tenantResolver.getDTO(authResult.getTenantRef());
        if (tenantDto == null)
            return null;

        final ApiKeyDTO apiKeyDto               = authResult.getApiKey();
        if (!authResponseUtil.isApiKeyAllowed(ctx, apiKeyDto))
            return null;

        final UserDTO userDto                 = (UserDTO) apiKeyDto.getUserRef();
        if (!authResponseUtil.isUserAllowedToLogOn(ctx, userDto))
            return null;

        final AuthenticationResponse resp = new AuthenticationResponse();
        resp.setJwtInfo(authResponseUtil.createJwt(apiKeyDto, tenantDto, userDto));
        resp.getJwtInfo().setLocale(locale);
        resp.getJwtInfo().setZoneinfo(zoneinfo);
        jwtEnrichment.enrichJwt(resp.getJwtInfo(), tenantDto, userDto, apiKeyDto);

        resp.setTenantName(tenantDto.getName());
        resp.setPasswordExpires(authResult.getAuthExpires());
        if (authResult.getUserStatus() != null) {
            resp.setLastLoginUser(authResult.getUserStatus().getPrevLogin());
            resp.setLastLoginMethod(authResult.getUserStatus().getPrevLoginByApiKey());
        }
        return resp;
    }

    protected AuthenticationResponse authDefault(final RequestContext ctx, final AuthenticationParameters unknown, final String locale, final String zoneinfo) {
        return otherAuthentication.auth(ctx, unknown, locale, zoneinfo);
    }

    protected AuthenticationResponse auth(final RequestContext ctx, final AuthenticationParameters ap, final String locale, final String zoneinfo) {
        if (ap instanceof ApiKeyAuthentication) {
          return authApiKeyAuthentication(ctx, (ApiKeyAuthentication)ap, locale, zoneinfo);
        } else if (ap instanceof PasswordAuthentication) {
          return authPasswordAuthentication(ctx, (PasswordAuthentication)ap, locale, zoneinfo);
        } else if (ap != null) {
          return authDefault(ctx, ap, locale, zoneinfo);
        } else {
          throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.<Object>asList(ctx, ap, locale, zoneinfo).toString());
        }
    }
}
