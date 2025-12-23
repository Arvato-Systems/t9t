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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.authc.api.ResetPasswordRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.UiConfigurationProvider;
import com.arvatosystems.t9t.zkui.viewmodel.AbstractViewOnlyVM;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

public class ForgotPasswordViewModel28 extends AbstractViewOnlyVM<ResetPasswordRequest, TrackingBase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForgotPasswordViewModel28.class);

    private final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);
    private final ApplicationConfigurationInitializer initializer = new ApplicationConfigurationInitializer();

    @Init
    void init() {
        super.setInitial("resetPwd");
    }

    @NotifyChange("data")
    @Command
    public void saveData() throws ReturnCodeException {
        final UUID forgetPasswordApiKey = initializer.getForgetPasswordApiKey();
        if (forgetPasswordApiKey == null) {
            throw new T9tException(T9tException.NOT_AUTHORIZED, "Configuration missing");
        } else {
            try {
                userDAO.getApiKeyAuthenticationResponse(forgetPasswordApiKey);
                userDAO.resetPassword(data.getUserId(), data.getEmailAddress());
            } finally {
                final ApplicationSession applicationSession = ApplicationSession.get();
                // if session has a JWT, clear it
                if (applicationSession.getEncodedJwt() != null) {
                    try {
                        // inform server about session logout
                        userDAO.sessionLogout(applicationSession.getEncodedJwt());
                    } catch (ReturnCodeException e) {
                        LOGGER.warn("Forgot password API key session logout failed: {}", e.getMessage());
                    }
                }
                applicationSession.setJwt(null);
            }
            postProcessHook();
        }
    }

    public void postProcessHook() {
        Messagebox.show(ApplicationSession.get().translate("resetPwd", "success"), ApplicationSession.get().translate("login", "title"), Messagebox.OK, null,
                new EventListener<Event>() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        Executions.getCurrent().sendRedirect(Constants.ZulFiles.LOGIN);
                    }
                });
    }

    @Command
    @NotifyChange("data")
    public void reset() {
        clearData();
    }

    private static class ApplicationConfigurationInitializer {
        private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfigurationInitializer.class);

        private final UUID forgetPasswordApiKey;

        private UUID fromStringOrNull(final String apiKeyStr) {
            if (apiKeyStr == null) {
                LOGGER.error("No API-KEY present!");
                return null;
            }
            try {
                return UUID.fromString(apiKeyStr);
            } catch (final Exception e) {
                LOGGER.error("Specified ResetPassword-API-KEY is not a valid UUID!");
            }
            return null;
        }

        ApplicationConfigurationInitializer() {
            LOGGER.debug("Trying to retrieve passwordReset API KEY");
            final String apiKeyStr = UiConfigurationProvider.getProperty("forget.password.api.key");
            forgetPasswordApiKey = fromStringOrNull(apiKeyStr);
        }

        public UUID getForgetPasswordApiKey() {
            return forgetPasswordApiKey;
        }
    }
}
