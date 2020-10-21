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
package com.arvatosystems.t9t.tfi.services;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.TimeZones;
import org.zkoss.util.resource.Labels;

import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.viewmodel.LoginViewModel;
import com.arvatosystems.t9t.tfi.viewmodel.LoginViewModel.UserInfo;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;
import com.arvatosystems.t9t.tfi.web.security.PasswordUtils;
import com.arvatosystems.t9t.authc.api.GetTenantsRequest;
import com.arvatosystems.t9t.authc.api.GetTenantsResponse;
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest;
import com.arvatosystems.t9t.authc.api.SwitchLanguageRequest;
import com.arvatosystems.t9t.authc.api.SwitchTenantRequest;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.authz.api.QueryPermissionsRequest;
import com.arvatosystems.t9t.authz.api.QueryPermissionsResponse;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.services.T9TRemoteUtils;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;


/**
 * Authentication and Authorization.
 * @author INCI02
 *
 */
@Singleton
public class UserDAO implements IUserDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    protected final T9TRemoteUtils t9tRemoteUtils = Jdp.getRequired(T9TRemoteUtils.class);
    protected final String UI_VERSION = IUserDAO.class.getPackage().getImplementationVersion();

    protected SessionParameters makeSessionParameters(String userName) {
        // currently the session is a new one, the attributes as set in login form are lost
        String mainAgent = "t9t ZK UI" + (UI_VERSION != null ? ", " + UI_VERSION : "");
        //LOGGER.debug("ZK user agent is {}", Executions.getCurrent().getUserAgent());
        SessionParameters sp = new SessionParameters();

        Locale   l = null;
        TimeZone z = null;
        String   realZoneId = null;
        UserInfo infos = LoginViewModel.getUserInfo(userName);
        if (infos != null) {
            l = infos.locale;
            z = infos.zkTz;
            realZoneId = infos.browserTz;
            if (infos.screenInfo != null)
                mainAgent = mainAgent + " @ " + infos.screenInfo;
        }
        if (mainAgent.length() > 255)
            mainAgent = mainAgent.substring(0, 255);
        sp.setLocale   (l == null ? ZulUtils.getDefaultLanguageCode() : l.toString());
        sp.setZoneinfo(realZoneId != null ? realZoneId : (z == null ? TimeZones.getCurrent() : z).toZoneId().toString());
        sp.setUserAgent(mainAgent);
        return sp;
    }
    @Override
    public final AuthenticationResponse getAuthenticationResponse( String username, String pwd) throws ReturnCodeException{
        try {

            AuthenticationRequest authenticationRequest = new AuthenticationRequest();

            if (username.length() >= 36 && pwd == null) {  // by API key (for password forgotten)
                authenticationRequest.setAuthenticationParameters(new ApiKeyAuthentication(UUID.fromString(username)));
                username = T9tConstants.ANONYMOUS_USER_ID;
            } else {
                if (pwd != null) { // in case of LDAP .... pwd is null
                    PasswordAuthentication authenticationParameters = new PasswordAuthentication();
                    authenticationParameters.setUserId(username);
                    authenticationParameters.setPassword(pwd);
                    authenticationRequest.setAuthenticationParameters(authenticationParameters);
                }
            }
            authenticationRequest.setSessionParameters(makeSessionParameters(username));
            AuthenticationResponse resp = t9tRemoteUtils.executeAndHandle(authenticationRequest, AuthenticationResponse.class);
            if (resp.getReturnCode() == 0) {
                final ApplicationSession as = ApplicationSession.get();
                as.setLastLoggedIn(resp.getLastLoginUser());
                as.setPasswordExpires(resp.getPasswordExpires());
                as.setNumberOfIncorrentAttempts(resp.getNumberOfIncorrentAttempts());
                if (resp.getTenantNotUnique()) {
                    // request all tenants via additional remote call...
                    LOGGER.info("User {} has access to multiple tenants - retrieving list", username);
                    GetTenantsResponse gtr = t9tRemoteUtils.executeAndHandle(new GetTenantsRequest(), GetTenantsResponse.class);
                    as.setAllowedTenants(gtr.getTenants());
                    LOGGER.info("User {} has access to {} tenants", username, gtr.getTenants().size());
                } else {
                    // this is the single tenant - no need to request more
                    LOGGER.info("User {} has access to the single tenant {} only", username, resp.getJwtInfo().getTenantId());
                    TenantDescription td = new TenantDescription();
                    td.setIsActive(true);
                    td.setName(resp.getTenantName());
                    td.setTenantId(resp.getJwtInfo().getTenantId());
                    td.setTenantRef(resp.getJwtInfo().getTenantRef());
                    as.setAllowedTenants(Collections.singletonList(td));

                    // obtain permissions
                }
            }
            return resp;

        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("security.bon#AuthenticationRequest", e);
            return null; // just for the compiler
        }
    }

    @Override
    public List<PermissionEntry> getPermissions() throws ReturnCodeException {
        try {
            return t9tRemoteUtils.executeAndHandle(
                new QueryPermissionsRequest(com.arvatosystems.t9t.base.auth.PermissionType.FRONTEND),
                QueryPermissionsResponse.class
            ).getPermissions();
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("security.bon#QueryPermissionsRequest", e);
            return null; // just for the compiler
        }
    }

    @Override
    public final void changePassword(String oldPassword, String newPassword) throws ReturnCodeException{
        try {
            // Check the new pwd is in valid format
            PasswordUtils passwordUtils = new PasswordUtils(Integer.valueOf(Labels.getLabel("pwd.length")),
                    Integer.valueOf(Labels.getLabel("pwd.lower")), Integer.valueOf(Labels.getLabel("pwd.upper")),
                    Integer.valueOf(Labels.getLabel("pwd.digit")), Integer.valueOf(Labels.getLabel("pwd.special")),
                    Boolean.valueOf(Labels.getLabel("pwd.checkIsoControl")));
            if (!passwordUtils.verifyPasswordStrength(null, newPassword)) {
                LOGGER.warn("Error while changing password. Remember that the password should contain upper and lower case characters as well as digits and special characters. "+ passwordUtils.toString());
                throw new ReturnCodeException(Constants.ErrorCodes.AUTHENTICATION_EXCEPTION, Labels.getLabel("err.pwd.requirements"), null);
            }
            String userId = ApplicationSession.get().getUserId();
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
}
