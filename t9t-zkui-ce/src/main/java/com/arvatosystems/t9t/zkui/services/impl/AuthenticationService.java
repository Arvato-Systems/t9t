/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import com.arvatosystems.t9t.auth.T9tAuthException;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.zkui.azure.ad.AadConstants;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IAuthenticationService;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;

@Singleton
public class AuthenticationService implements IAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    protected final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    @Override
    public void login(final String username, final String password) throws T9tException {
        LOGGER.debug("Login user '{}'", username);
        try {
            final AuthenticationResponse authResponse = userDAO.getUserPwAuthenticationResponse(username, password);
            loginSuccessRedirect();
        } catch (final ReturnCodeException e) {
            LOGGER.debug("Login user '{}' failed with message: {}", username, e.getMessage());
            LOGGER.warn("May Missing object data for AllowedTenants/UserHistory/UserInformation");
            throw new T9tException(T9tAuthException.LOGIN_FAILED);
        } catch (final Exception e) {
            LOGGER.debug("Login user '{}' failed with message: {}", username, e.getMessage());
            throw new T9tException(T9tAuthException.LOGIN_FAILED, e);
        }
    }

    @Override
    public void loginWithExternalToken(final String accessToken, final String username) throws T9tException {
        LOGGER.debug("Login user with external token '{}'", username);
        try {
            userDAO.getExternalTokenAuthenticationResponse(accessToken, username);
            LOGGER.debug("Login successful, redirect to login success page");
            Executions.getCurrent().sendRedirect(Constants.ZulFiles.LOGIN_TENANT_SELECTION);
        } catch (final ReturnCodeException e) {
            LOGGER.debug("Login user '{}' failed with message: {}", username, e.getMessage());
            LOGGER.warn("May Missing object data for AllowedTenants/UserHistory/UserInformation");
            throw new T9tException(T9tAuthException.LOGIN_FAILED);
        } catch (final Exception e) {
            LOGGER.debug("Login user '{}' failed with message: {}", username, e.getMessage());
            throw new T9tException(T9tAuthException.LOGIN_FAILED, e);
        }
    }

    @Override
    public void logout() {
        final ApplicationSession applicationSession = ApplicationSession.get();
        final boolean msLogin = applicationSession.getSessionValue(AadConstants.SESSION_PARAM) != null;   // Login with Microsoft
        if (applicationSession.isAuthenticated()) {
            LOGGER.debug("Logout user '{}'", applicationSession.getJwtInfo().getUserId());
            applicationSession.invalidateSession();
        }
        if (msLogin) {
            final String redirect = AadConstants.AUTHORITY + AadConstants.SIGN_OUT_ENDPOINT + AadConstants.POST_SIGN_OUT_FRAGMENT + AadConstants.HOME_PAGE
                + Constants.ZulFiles.LOGIN;
            Executions.sendRedirect(redirect);
        } else {
            logoutSuccessRedirect();
        }
    }

    protected void loginSuccessRedirect() {
        LOGGER.debug("Login successful, redirect to login success page");
        Executions.sendRedirect(Constants.ZulFiles.LOGIN_SUCCESS_REDIRECT);
    }

    protected void logoutSuccessRedirect() {
        LOGGER.debug("Logout successful, redirect to logout success page");
        Executions.sendRedirect(Constants.ZulFiles.LOGIN);
    }
}
