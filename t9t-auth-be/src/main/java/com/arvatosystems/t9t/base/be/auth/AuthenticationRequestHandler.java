/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import java.time.Instant;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.AuthenticationIssuerType;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment;
import com.arvatosystems.t9t.auth.hooks.IOtherAuthentication;
import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.auth.services.AuthIntermediateResult;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.auth.services.IAuthResponseUtil;
import com.arvatosystems.t9t.auth.services.IExternalAuthentication;
import com.arvatosystems.t9t.auth.services.ITenantResolver;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.ExternalTokenAuthenticationParam;
import com.arvatosystems.t9t.base.auth.HighRiskNotificationType;
import com.arvatosystems.t9t.base.auth.JwtAuthentication;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IHighRiskSituationNotificationService;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.LdapConfiguration;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class AuthenticationRequestHandler extends AbstractRequestHandler<AuthenticationRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationRequestHandler.class);
    private final IAuthPersistenceAccess persistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);
    private final IAuthResponseUtil authResponseUtil = Jdp.getRequired(IAuthResponseUtil.class);
    private final ITenantResolver tenantResolver = Jdp.getRequired(ITenantResolver.class);
    private final IJwtEnrichment jwtEnrichment = Jdp.getRequired(IJwtEnrichment.class);
    private final IExternalAuthentication externalAuthentication = Jdp.getRequired(IExternalAuthentication.class);
    private final IOtherAuthentication otherAuthentication = Jdp.getRequired(IOtherAuthentication.class);
    private final IJWT jwt = Jdp.getRequired(IJWT.class);
    private final IHighRiskSituationNotificationService hrSituationNotificationService = Jdp.getRequired(IHighRiskSituationNotificationService.class);
    private final IAuthModuleCfgDtoResolver moduleCfgResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);

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
          || resp.getJwtInfo() == null) {
            throw new ApplicationException(T9tException.T9T_ACCESS_DENIED);
        }
        final JwtInfo jwtInfo = resp.getJwtInfo();
        if (rq.getSessionParameters() != null) {
            jwtInfo.setLocale(rq.getSessionParameters().getLocale());
            jwtInfo.setZoneinfo(rq.getSessionParameters().getZoneinfo());
        }
        resp.setMustChangePassword(resp.getPasswordExpires() != null && resp.getPasswordExpires().isBefore(Instant.now()));
        resp.setEncodedJwt(authResponseUtil.authResponseFromJwt(jwtInfo, rq.getSessionParameters(), null, resp.getApiKeyRef()));
        resp.setNumberOfIncorrectAttempts(resp.getNumberOfIncorrectAttempts());
        resp.setTenantNotUnique(resp.getJwtInfo().getTenantId().equals(T9tConstants.GLOBAL_TENANT_ID)); // only then the user has access to additional ones
        LOGGER.debug("User {} successfully logged in for tenant {} via {}", resp.getJwtInfo().getUserId(), resp.getJwtInfo().getTenantId(),
          resp.getApiKeyRef() != null ? "API key" : "user/PW");

        return resp;
    }

    /** Authenticates a user via userId / password. Relevant information for the JWT is taken from the UserDTO, then the TenantDTO. */
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
            final DataWithTrackingS<UserDTO, FullTrackingWithVersion> user = persistenceAccess.getUserById(pw.getUserId());
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

        // only for password change case
        if (T9tUtil.isNotBlank(pw.getPassword()) && T9tUtil.isNotBlank(pw.getNewPassword())) {
            final AuthModuleCfgDTO moduleCfg = moduleCfgResolver.getModuleConfiguration();
            if (Boolean.TRUE.equals(moduleCfg.getNotifyPasswordChange())) {
                hrSituationNotificationService.notifyChange(ctx, HighRiskNotificationType.PASSWORD_CHANGE.name(), userDto.getUserId(), userDto.getName(), userDto.getEmailAddress(), null);
            }
        }

        final TenantDTO tenantDto = tenantResolver.getDTO(authResult.getTenantId());
        final AuthenticationResponse resp = new AuthenticationResponse();
        resp.setJwtInfo(authResponseUtil.createJwt(userDto, tenantDto, AuthenticationIssuerType.ISSUER_USERID_PASSWORD));
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
        final TenantDTO tenantDto = tenantResolver.getDTO(authResult.getTenantId());
        if (tenantDto == null)
            return null;

        final ApiKeyDTO apiKeyDto = authResult.getApiKey();
        if (!authResponseUtil.isApiKeyAllowed(ctx, apiKeyDto)) {
            return null;
        }

        final UserDTO userDto = (UserDTO) apiKeyDto.getUserRef();
        if (!authResponseUtil.isUserAllowedToLogOn(ctx, userDto)) {
            return null;
        }

        final AuthenticationResponse resp = new AuthenticationResponse();
        resp.setJwtInfo(authResponseUtil.createJwt(apiKeyDto, tenantDto, userDto));
        resp.getJwtInfo().setLocale(locale);
        resp.getJwtInfo().setZoneinfo(zoneinfo);
        resp.setApiKeyRef(apiKeyDto.getObjectRef());
        jwtEnrichment.enrichJwt(resp.getJwtInfo(), tenantDto, userDto, apiKeyDto);

        resp.setTenantName(tenantDto.getName());
        resp.setPasswordExpires(authResult.getAuthExpires());
        if (authResult.getUserStatus() != null) {
            resp.setLastLoginUser(authResult.getUserStatus().getPrevLogin());
            resp.setLastLoginMethod(authResult.getUserStatus().getPrevLoginByApiKey());
        }
        return resp;
    }

    /** Authenticates a user via external access token. Relevant information for the JWT is taken from the UserDTO, finally the TenantDTO. */
    protected AuthenticationResponse authExternalTokenAuthentication(final RequestContext ctx, final ExternalTokenAuthenticationParam authParam,
            final String locale, final String zoneinfo) {
        final AuthIntermediateResult authResult = persistenceAccess.getByExternalToken(ctx.executionStart, authParam);
        if (authResult == null || !ApplicationException.isOk(authResult.getReturnCode())) {
            LOGGER.debug("Incorrect authentication with external token");
            return null;
        }
        final UserDTO userDto = authResult.getUser();
        if (!authResponseUtil.isUserAllowedToLogOn(ctx, userDto)) {
            return null;
        }
        final String tenantId = authResult.getTenantId();
        final TenantDTO tenantDto = tenantResolver.getDTO(tenantId);
        if (tenantDto == null) {
            LOGGER.debug("Tenant not found for tenantId {} and user {}", tenantId, userDto == null ? "(NULL)" : userDto.getUserId());
        }
        final AuthenticationResponse resp = new AuthenticationResponse();
        resp.setJwtInfo(authResponseUtil.createJwt(userDto, tenantDto, AuthenticationIssuerType.ISSUER_USERID_MS_OPENID));
        resp.getJwtInfo().setLocale(locale);
        resp.getJwtInfo().setZoneinfo(zoneinfo);
        jwtEnrichment.enrichJwt(resp.getJwtInfo(), tenantDto, userDto);
        resp.setTenantId(tenantDto.getTenantId());
        resp.setTenantName(tenantDto.getName());
        if (authResult.getUserStatus() != null) {
            resp.setLastLoginUser(authResult.getUserStatus().getPrevLogin());
        }
        return resp;
    }

    private AuthenticationResponse authJwtAuthentication(final RequestContext ctx, final JwtAuthentication jwtAp, final String locale, final String zoneinfo) {
        final AuthenticationResponse resp = new AuthenticationResponse();

        String jwtToken = jwtAp.getEncodedJwt();
        JwtInfo jwtInfo = jwt.decode(jwtToken);
        final TenantDTO tenantDto = tenantResolver.getDTO(jwtInfo.getTenantId());

        // jwtInfo is frozen - but setter are working on it in following methods
        resp.setJwtInfo(jwtInfo.ret$MutableClone(true, true));
        resp.setEncodedJwt(jwtToken);
        resp.setTenantId(jwtInfo.getTenantId());
        resp.setTenantName(tenantDto.getName());

        return resp;
    }

    protected AuthenticationResponse authDefault(final RequestContext ctx, final AuthenticationParameters unknown, final String locale, final String zoneinfo) {
        return otherAuthentication.auth(ctx, unknown, locale, zoneinfo);
    }

    protected AuthenticationResponse auth(final RequestContext ctx, final AuthenticationParameters ap, final String locale, final String zoneinfo) {
        if (ap instanceof ApiKeyAuthentication akAp) {
            return authApiKeyAuthentication(ctx, akAp, locale, zoneinfo);
        } else if (ap instanceof PasswordAuthentication pwAp) {
            return authPasswordAuthentication(ctx, pwAp, locale, zoneinfo);
        } else if (ap instanceof JwtAuthentication jwtAp) {
            return authJwtAuthentication(ctx, jwtAp, locale, zoneinfo);
        } else if (ap instanceof ExternalTokenAuthenticationParam extTokenAp) {
            return authExternalTokenAuthentication(ctx, extTokenAp, locale, zoneinfo);
        } else if (ap != null) {
            return authDefault(ctx, ap, locale, zoneinfo);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.<Object>asList(ctx, ap, locale, zoneinfo).toString());
        }
    }
}
