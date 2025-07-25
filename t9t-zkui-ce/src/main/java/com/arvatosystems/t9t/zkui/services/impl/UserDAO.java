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
package com.arvatosystems.t9t.zkui.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import com.arvatosystems.t9t.zkui.session.UserInfo;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.TimeZones;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.auth.request.GetPasswordChangeRequirementsRequest;
import com.arvatosystems.t9t.auth.request.GetPasswordChangeRequirementsResponse;
import com.arvatosystems.t9t.auth.request.UserCrudAndSetPasswordRequest;
import com.arvatosystems.t9t.authc.api.GetTenantsRequest;
import com.arvatosystems.t9t.authc.api.GetTenantsResponse;
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest;
import com.arvatosystems.t9t.authc.api.SwitchLanguageRequest;
import com.arvatosystems.t9t.authc.api.SwitchTenantRequest;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.authz.api.QueryPermissionsRequest;
import com.arvatosystems.t9t.authz.api.QueryPermissionsResponse;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.ExternalTokenAuthenticationParam;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.ZulUtils;
import com.arvatosystems.t9t.zkui.viewmodel.support.LoginViewModel;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;


/**
 * Authentication and Authorization.
 * @author INCI02
 *
 */
@Singleton
public class UserDAO implements IUserDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    protected final IT9tRemoteUtils t9tRemoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);
    protected static final String UI_VERSION = IUserDAO.class.getPackage().getImplementationVersion();

    protected SessionParameters makeSessionParameters(String userName) {
        // get userInfo from internal cache to make session params
        return makeSessionParameters(LoginViewModel.getUserInfo(userName));
    }

    protected SessionParameters makeSessionParameters(@Nullable final UserInfo infos) {
        // currently the session is a new one, the attributes as set in login form are lost
        String mainAgent = "t9t ZK UI" + (UI_VERSION != null ? ", " + UI_VERSION : "");
        final SessionParameters sp = new SessionParameters();

        Locale   l = null;
        TimeZone z = null;
        String   realZoneId = null;
        if (infos != null) {
            l = infos.locale();
            z = infos.zkTz();
            realZoneId = infos.browserTz();
            if (infos.screenInfo() != null)
                mainAgent = mainAgent + " @ " + infos.screenInfo();
        }
        if (mainAgent.length() > 255)
            mainAgent = mainAgent.substring(0, 255);
        sp.setLocale   (l == null ? ZulUtils.getDefaultLanguageCode() : l.toString());
        sp.setZoneinfo(realZoneId != null ? realZoneId : (z == null ? TimeZones.getCurrent() : z).toZoneId().toString());
        sp.setUserAgent(mainAgent);
        return sp;
    }

    @Override
    public AuthenticationResponse getApiKeyAuthenticationResponse(final UUID apiKey) throws ReturnCodeException {
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest(new ApiKeyAuthentication(apiKey));
        return getAuthenticationResponse(authenticationRequest, T9tConstants.ANONYMOUS_USER_ID, true);
    }

    @Override
    public AuthenticationResponse getUserPwAuthenticationResponse(final String username, final String pwd) throws ReturnCodeException {
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        if (pwd != null) { // in case of LDAP .... pwd is null
            final PasswordAuthentication authenticationParameters = new PasswordAuthentication();
            authenticationParameters.setUserId(username);
            authenticationParameters.setPassword(pwd);
            authenticationRequest.setAuthenticationParameters(authenticationParameters);
        }
        return getAuthenticationResponse(authenticationRequest, username, true);
    }

    @Override
    public final AuthenticationResponse getExternalTokenAuthenticationResponse(final String accessToken, final String username, final UserInfo userSessionInfo)
        throws ReturnCodeException {
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest(new ExternalTokenAuthenticationParam(accessToken));
        authenticationRequest.setSessionParameters(makeSessionParameters(userSessionInfo));
        return getAuthenticationResponse(authenticationRequest, username, false);
    }

    @Override
    public List<PermissionEntry> getPermissions() throws ReturnCodeException {
        try {
            return t9tRemoteUtils.executeAndHandle(
                new QueryPermissionsRequest(PermissionType.FRONTEND),
                QueryPermissionsResponse.class
            ).getPermissions();
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("security.bon#QueryPermissionsRequest", e);
            return null; // just for the compiler
        }
    }

    @Override
    public final void changePassword(String oldPassword, String newPassword) throws ReturnCodeException {
        try {
            final ApplicationSession as = ApplicationSession.get();
            String userId = as.getUserId();
            AuthenticationRequest changePasswordRequest = new AuthenticationRequest();

            PasswordAuthentication authenticationParameters = new PasswordAuthentication();
            authenticationParameters.setPassword(oldPassword);
            authenticationParameters.setNewPassword(newPassword);
            authenticationParameters.setUserId(userId);



            changePasswordRequest.setAuthenticationParameters(authenticationParameters);
            changePasswordRequest.setSessionParameters(makeSessionParameters(userId));

            t9tRemoteUtils.executeAndHandle(changePasswordRequest, ServiceResponse.class);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("security.bon#ChangePasswordRequest", e);
        }
    }


    @Override
    public final void resetPassword(String userId, String emailAddress) throws ReturnCodeException {
        try {
            ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
            resetPasswordRequest.setEmailAddress(emailAddress);
            resetPasswordRequest.setUserId(userId);
            t9tRemoteUtils.executeAndHandle(resetPasswordRequest, ServiceResponse.class);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("security.bon#ResetPasswordRequest", e);
        }
    }


    @Override
    public AuthenticationResponse switchTenant(String tenantId) throws ReturnCodeException {

        AuthenticationResponse response =  null;
        try {
            SwitchTenantRequest switchTenantRequest =  new SwitchTenantRequest();
            switchTenantRequest.setTenantId(tenantId);
            response = t9tRemoteUtils.executeAndHandle(switchTenantRequest, AuthenticationResponse.class);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("api-key.bon#SwitchTenantRequest", e);
        }
        return response;
    }

    @Override
    public AuthenticationResponse switchLanguage(String language) throws ReturnCodeException {

        AuthenticationResponse response =  null;
        try {
            SwitchLanguageRequest switchTenantRequest =  new SwitchLanguageRequest();
            switchTenantRequest.setLanguage(language);
            response = t9tRemoteUtils.executeAndHandle(switchTenantRequest, AuthenticationResponse.class);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("api-key.bon#SwitchLanguageRequest", e);
        }
        return response;
    }

    @Override
    public GetPasswordChangeRequirementsResponse getPasswordChangeRequirements() throws ReturnCodeException {
        GetPasswordChangeRequirementsResponse response = null;
        try {
            final GetPasswordChangeRequirementsRequest request = new GetPasswordChangeRequirementsRequest();
            response = t9tRemoteUtils.executeAndHandle(request, GetPasswordChangeRequirementsResponse.class);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("api-key.bon#SwitchLanguageRequest", e);
        }
        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion> setPassword(final UserDTO user, final String password) throws ReturnCodeException {
        CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion> response = null;

        try {
            final UserCrudAndSetPasswordRequest request = new UserCrudAndSetPasswordRequest();
            request.setPassword(password);
            request.setCrud(OperationType.MERGE);
            request.setData(user);
            UserKey userKey = new UserKey(user.getUserId());
            request.setNaturalKey(userKey);
            response = t9tRemoteUtils.executeAndHandle(request, CrudSurrogateKeyResponse.class);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("security.bon#UserCrudAndSetPasswordRequest", e);
        }

        return response;
    }

    private AuthenticationResponse getAuthenticationResponse(final AuthenticationRequest authenticationRequest, final String username,
        final boolean populateSession) throws ReturnCodeException {
        try {
            if (populateSession) {
                authenticationRequest.setSessionParameters(makeSessionParameters(username));
            }
            final AuthenticationResponse resp = t9tRemoteUtils.executeAndHandle(authenticationRequest, AuthenticationResponse.class);
            if (ApplicationException.isOk(resp.getReturnCode()) || resp.getReturnCode() == T9tException.PASSWORD_EXPIRED) {
                final ApplicationSession as = ApplicationSession.get();
                as.setPasswordExpired(resp.getReturnCode() == T9tException.PASSWORD_EXPIRED);
                as.setLastLoggedIn(resp.getLastLoginUser());
                as.setPasswordExpires(resp.getPasswordExpires());
                as.setNumberOfIncorrectAttempts(resp.getNumberOfIncorrectAttempts());
                if (resp.getTenantNotUnique()) {
                    // request all tenants via additional remote call...
                    LOGGER.info("User {} has access to multiple tenants - retrieving list", username);
                    final GetTenantsResponse gtr = t9tRemoteUtils.executeAndHandle(new GetTenantsRequest(), GetTenantsResponse.class);
                    as.setAllowedTenants(gtr.getTenants());
                    LOGGER.info("User {} has access to {} tenants", username, gtr.getTenants().size());
                } else {
                    // this is the single tenant - no need to request more
                    LOGGER.info("User {} has access to the single tenant {} only", username, resp.getJwtInfo().getTenantId());
                    final TenantDescription td = new TenantDescription();
                    td.setIsActive(true);
                    td.setName(resp.getTenantName());
                    td.setTenantId(resp.getJwtInfo().getTenantId());
                    as.setAllowedTenants(Collections.singletonList(td));
                }
            }
            return resp;
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("security.bon#AuthenticationRequest", e);
            return null; // just for the compiler
        }
    }
}
