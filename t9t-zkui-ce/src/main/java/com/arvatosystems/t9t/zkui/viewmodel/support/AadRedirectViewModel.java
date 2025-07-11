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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

import com.arvatosystems.t9t.zkui.azure.ad.AadAuthUtil;
import com.arvatosystems.t9t.zkui.azure.ad.AadConstants;
import com.arvatosystems.t9t.zkui.azure.ad.IdentityContextData;
import com.arvatosystems.t9t.zkui.services.IAuthenticationService;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import de.jpaw.dp.Jdp;

public class AadRedirectViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadRedirectViewModel.class);

    private final IAuthenticationService authenticationService = Jdp.getRequired(IAuthenticationService.class);

    @Init
    public void init() {
        LOGGER.debug("AAD auth redirect request received");
        final Execution currentExecution = Executions.getCurrent();
        final ApplicationSession applicationSession = ApplicationSession.get();
        final IdentityContextData aadContextData = (IdentityContextData) applicationSession.getSessionValue(AadConstants.SESSION_PARAM);

        if (aadContextData == null) {
            LOGGER.debug("Received AAD auth redirect but no context data found");
            redirectToLogin(false);
            return;
        }
        if (!validateState(currentExecution, aadContextData) || hasError(currentExecution)) {
            redirectToLogin(true);
            return;
        }

        final String authCode = currentExecution.getParameter("code");
        if (authCode == null) {
            LOGGER.debug("No auth code received in AAD auth redirect");
            redirectToLogin(true);
            return;
        }

        try {
            final AuthorizationCodeParameters authParams = AuthorizationCodeParameters.builder(authCode, new URI(AadConstants.REDIRECT_URI))
                .scopes(Collections.singleton(AadConstants.SCOPES)).build();

            final ConfidentialClientApplication client = AadAuthUtil.getConfidentialClientInstance();
            final IAuthenticationResult result = client.acquireToken(authParams).get();

            aadContextData.populateIdTokenAndClaims(result.idToken());
            if (!validateNonce(aadContextData)) {
                redirectToLogin(true);
                return;
            }
            aadContextData.setAuthResult(result);

            authenticationService.loginWithExternalToken(aadContextData.getAccessToken(), aadContextData.getUsername(), aadContextData.getUserInfo());

        } catch (final Exception ex) {
            LOGGER.error("Unable to process AAD auth redirect. Cause {}", ex);
            redirectToLogin(true);
        }
    }

    private boolean validateState(final Execution execution, final IdentityContextData aadContextData) {
        LOGGER.debug("validating state for AAD auth redirect");
        final String requestState = execution.getParameter("state");
        final String sessionState = aadContextData.getState();
        if (requestState == null || !requestState.equals(sessionState)
            || aadContextData.getStateDate().isBefore(LocalDateTime.now().minusMinutes(AadConstants.STATE_TTL))) {
            LOGGER.debug("Invalid state set in AAD auth redirect");
            return false;
        }
        aadContextData.setState(null);
        return true;
    }

    private boolean validateNonce(final IdentityContextData aadContextData) {
        final String nonceClaim = (String) aadContextData.getIdTokenClaims().get("nonce");
        final String sessionNonce = aadContextData.getNonce();

        if (nonceClaim == null || !nonceClaim.equals(sessionNonce)) {
            LOGGER.debug("Invalid nonce received in AAD auth redirect");
            return false;
        }
        aadContextData.setNonce(null);
        return true;
    }

    private boolean hasError(final Execution execution) {
        LOGGER.debug("Checking for errors in AAD auth redirect");
        final String error = execution.getParameter("error");
        final String errorDescription = execution.getParameter("error_description");
        if (error != null || errorDescription != null) {
            LOGGER.error("Error found in AAD auth redirect. Error: {}, Description: {}", error, errorDescription);
            return true;
        }
        return false;
    }

    private void redirectToLogin(boolean fail) {
        Executions.sendRedirect(Constants.ZulFiles.LOGIN + (fail ? "?loginFail=true" : ""));
    }
}
