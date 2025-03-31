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
package com.arvatosystems.t9t.auth.be.impl;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.ApiKeyRef;
import com.arvatosystems.t9t.auth.AuthenticationIssuerType;
import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.SessionDTO;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.auth.hooks.IJwtEnrichment;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.auth.services.IAuthResponseUtil;
import com.arvatosystems.t9t.auth.services.IAuthenticator;
import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AuthResponseUtil implements IAuthResponseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResponseUtil.class);

    public static final Long   DEFAULT_JWT_VALIDITY_AK = 12L * 60 * 60;
    public static final Long   DEFAULT_JWT_VALIDITY_UP = 12L * 60 * 60;
    public static final Long   DEFAULT_JWT_VALIDITY_MS =  6L * 60 * 60;
    public static final Long   DEFAULT_JWT_VALIDITY    =  1L * 60 * 60;  // fallback / unknown

    private final IAuthenticator          authenticator     = Jdp.getRequired(IAuthenticator.class);
    private final IAuthPersistenceAccess  persistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);
    private final IJwtEnrichment          jwtEnrichment     = Jdp.getRequired(IJwtEnrichment.class);
    private final IRefGenerator           refGenerator      = Jdp.getRequired(IRefGenerator.class);

    final Long jwtValidityApiKey;
    final Long jwtValidityUserPassword;
    final Long jwtValidityOpenId;
    final boolean sessionLogSysout;

    public AuthResponseUtil() {
        final T9tServerConfiguration serverCfg = ConfigProvider.getConfiguration();
        jwtValidityApiKey       = serverCfg.getJwtValidityApiKey()       == null ? DEFAULT_JWT_VALIDITY_AK : serverCfg.getJwtValidityApiKey();
        jwtValidityUserPassword = serverCfg.getJwtValidityUserPassword() == null ? DEFAULT_JWT_VALIDITY_UP : serverCfg.getJwtValidityUserPassword();
        jwtValidityOpenId       = serverCfg.getJwtValidityOpenId()       == null ? DEFAULT_JWT_VALIDITY_MS : serverCfg.getJwtValidityOpenId();
        sessionLogSysout        = Boolean.TRUE.equals(serverCfg.getSessionLogSysout());
    }

    // common part of code for SwitchTenantRequest and AuthenticationRequest
    @Override
    public String authResponseFromJwt(final JwtInfo jwt, final SessionParameters sp, final JwtInfo continuesFromJwt, final Long apiKeyRef) {
        jwt.setSessionId(UUID.randomUUID());
        jwt.setSessionRef(refGenerator.generateRef(T9tInternalConstants.TABLENAME_SESSION, SessionDTO.class$rtti()));
        jwt.setResource(normalize(jwt.getResource())); // strip any leading or trailing spaces, and convert to null in case those were
                                                       // the only content
        // create a session
        final SessionDTO session = new SessionDTO();
        session.setSessionId(jwt.getSessionId());
        session.setObjectRef(jwt.getSessionRef());
        session.setLocale(jwt.getLocale());
        session.setZoneinfo(jwt.getZoneinfo());
        session.setUserRef(new UserRef(jwt.getUserRef()));
        session.setTenantId(jwt.getTenantId());
        session.setApiKeyRef(new ApiKeyRef(apiKeyRef));
        if (sp != null) {
            // memorize session parameters
            session.setLocale(sp.getLocale());
            session.setUserAgent(sp.getUserAgent());
            session.setDataUri(sp.getDataUri());
            session.setZoneinfo(sp.getZoneinfo());
        }
        if (continuesFromJwt != null) {
            // transition reference to prior session
            session.setContinuesFromSession(continuesFromJwt.getSessionRef());
        }

        if (UserLogLevelType.STEALTH != jwt.getLogLevel()) {
            // only if we do not want ANY info (such as from ping requests)
            if (sessionLogSysout) {
                // do not store session info to DB
                LOGGER.info("New session created: tenant {}, user {}, sessionRef {}", jwt.getTenantId(), jwt.getUserId(), jwt.getSessionRef());
            } else {
                persistenceAccess.storeSession(session);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("About to sign the following Jwt: {}", ToStringHelper.toStringML(jwt));
        } else {
            LOGGER.debug("About to sign a Jwt for user {}, tenant {}", jwt.getUserId(), jwt.getTenantId());
        }

        // determine the duration of validity
        final String issuerType = jwt.getIssuer();
        final Long duration;
        if (AuthenticationIssuerType.ISSUER_APIKEY.getToken().equals(issuerType)) {
            duration = jwtValidityApiKey;
        } else if (AuthenticationIssuerType.ISSUER_USERID_PASSWORD.getToken().equals(issuerType)) {
            duration = jwtValidityUserPassword;
        } else if (AuthenticationIssuerType.ISSUER_USERID_MS_OPENID.getToken().equals(issuerType)) {
            duration = jwtValidityOpenId;
        } else {
            duration = DEFAULT_JWT_VALIDITY;
        }

        return authenticator.doSign(jwt, duration);
    }

    @Override
    public JwtInfo createJwt(final UserDTO user, final TenantDTO tenantDTO, final AuthenticationIssuerType issuerType) {
        final PermissionsDTO p = user.getPermissions();
        final JwtInfo jwtInfo = new JwtInfo();
        jwtInfo.setIssuer(issuerType.getToken());
        jwtInfo.setRoleRef(user.getRoleRef() == null ? null : user.getRoleRef().getObjectRef());
        // same from here
        jwtInfo.setUserRef(user.getObjectRef());
        jwtInfo.setUserId(user.getUserId());
        jwtInfo.setName(user.getName());
        jwtInfo.setTenantId(tenantDTO.getTenantId());
        jwtInfo.setRoleRef(user.getRoleRef() == null ? null : user.getRoleRef().getObjectRef());
        jwtInfo.setZ(jwtEnrichment.mergeZs(user.getZ(), tenantDTO.getZ()));
        if (p != null) {
            jwtInfo.setPermissionsMin(p.getMinPermissions());
            jwtInfo.setPermissionsMax(p.getMaxPermissions());
            jwtInfo.setLogLevel(p.getLogLevel());
            jwtInfo.setLogLevelErrors(p.getLogLevelErrors());
            jwtInfo.setResource(p.getResourceRestriction());
            jwtInfo.setResourceIsWildcard(p.getResourceIsWildcard());
        }
        enrichJwtWithTenantDefaults(jwtInfo, tenantDTO.getPermissions());
        return jwtInfo;
    }

    @Override
    public JwtInfo createJwt(final ApiKeyDTO apiKey, final TenantDTO tenantDTO, final UserDTO userDTO) {
        final PermissionsDTO p = apiKey.getPermissions();
        final UserDTO user = (UserDTO) apiKey.getUserRef();
        final PermissionsDTO pu = user.getPermissions();
        final JwtInfo jwtInfo = new JwtInfo();
        jwtInfo.setIssuer(AuthenticationIssuerType.ISSUER_APIKEY.getToken());
        jwtInfo.setRoleRef(apiKey.getRoleRef() == null ? null : apiKey.getRoleRef().getObjectRef());
        // same from here
        jwtInfo.setUserRef(user.getObjectRef());
        jwtInfo.setUserId(user.getUserId());
        jwtInfo.setName(user.getName());
        jwtInfo.setTenantId(tenantDTO.getTenantId());
        jwtInfo.setRoleRef(apiKey.getRoleRef() == null
          ? user.getRoleRef() == null ? null : user.getRoleRef().getObjectRef()
          : apiKey.getRoleRef().getObjectRef());
        jwtInfo.setZ(jwtEnrichment.mergeZs(user.getZ(), tenantDTO.getZ()));

        if (p != null) {
            jwtInfo.setPermissionsMin(p.getMinPermissions() == null ? pu == null ? null : pu.getMinPermissions() : p.getMinPermissions());
            jwtInfo.setPermissionsMax(p.getMaxPermissions() == null ? pu == null ? null : pu.getMaxPermissions() : p.getMaxPermissions());
            jwtInfo.setLogLevel(p.getLogLevel());
            jwtInfo.setLogLevelErrors(p.getLogLevelErrors());
            jwtInfo.setResource(p.getResourceRestriction());
            jwtInfo.setResourceIsWildcard(p.getResourceIsWildcard());
        }

        enrichJwtWithTenantDefaults(jwtInfo, tenantDTO.getPermissions());
        return jwtInfo;
    }

    private String normalize(final String in) {
        if (in == null)
            return null;
        String trimmed = in.trim();
        if (trimmed.length() == 0)
            return null;
        return trimmed;
    }

    protected void enrichJwtWithTenantDefaults(final JwtInfo it, final PermissionsDTO pt) {
        if (pt != null) {
            if (it.getPermissionsMin() == null)
                it.setPermissionsMin(pt.getMinPermissions());
            if (it.getPermissionsMax() == null)
                it.setPermissionsMax(pt.getMaxPermissions());
            if (it.getLogLevel() == null)
                it.setLogLevel(pt.getLogLevel());
            if (it.getLogLevelErrors() == null)
                it.setLogLevelErrors(pt.getLogLevelErrors());
            // fallbacks for resource do not make sense
        }
    }

    @Override
    public boolean isApiKeyAllowed(final RequestContext ctx, final ApiKeyDTO apiKey) {
        if (!apiKey.getIsActive()) {
            LOGGER.debug("Authentication via API key {} denied, key is inactive", apiKey.getApiKey());
            return false;
        }
        if (apiKey.getPermissions() != null) {
            if (apiKey.getPermissions().getValidFrom() != null && apiKey.getPermissions().getValidFrom().isAfter(ctx.executionStart)) {
                LOGGER.debug("Authentication via API key {} denied, key is not allowed before {}", apiKey.getApiKey(), apiKey.getPermissions().getValidFrom());
                return false;
            }
            if (apiKey.getPermissions().getValidTo() != null && apiKey.getPermissions().getValidTo().isBefore(ctx.executionStart)) {
                LOGGER.debug("Authentication via API key {} denied, key is not allowed after {}", apiKey.getApiKey(), apiKey.getPermissions().getValidTo());
                return false;
            }
        }
        return true;  // all tests passed
    }

    @Override
    public boolean isUserAllowedToLogOn(final RequestContext ctx, final UserDTO userDto) {
        if (!userDto.getIsActive()) {
            LOGGER.debug("Authentication of userId {} denied, user is inactive", userDto.getUserId());
            return false;
        }
        if (userDto.getPermissions() != null) {
            if (userDto.getPermissions().getValidFrom() != null && userDto.getPermissions().getValidFrom().isAfter(ctx.executionStart)) {
                LOGGER.debug("Authentication of userId {} denied, user is not allowed before {}", userDto.getUserId(), userDto.getPermissions().getValidFrom());
                return false;
            }
            if (userDto.getPermissions().getValidTo() != null && userDto.getPermissions().getValidTo().isBefore(ctx.executionStart)) {
                LOGGER.debug("Authentication of userId {} denied, user is not allowed after {}", userDto.getUserId(), userDto.getPermissions().getValidTo());
                return false;
            }
        }
        return true;  // all tests passed
    }
}
