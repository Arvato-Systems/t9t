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
package com.arvatosystems.t9t.zkui.init;

import java.util.Map;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;

/**
 * This is a class which catches the initialization of a ZK page and
 * redirects accordingly if no user credentials are found
 */
public class WorkbenchInit implements Initiator {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkbenchInit.class);

    /**
     * Invoked when the ZK Parser starts.
     * @param page Page
     * @param arg Map
     * @throws Exception
     */
    @Override
    public final void doInit(Page page, @SuppressWarnings("rawtypes") Map arg) throws Exception {
        String pagename = (String)arg.get("pagename");

        final ApplicationSession as = ApplicationSession.get();
        if (!as.isAuthenticated()) {
            LOGGER.info("User is not authenticated, redirecting to login...");
            Executions.getCurrent().sendRedirect(Constants.ZulFiles.LOGIN);
        } else if (as.isPasswordExpired()) {
            LOGGER.info("User logged in but password is expired, redirecting to change password...");
            Executions.sendRedirect(Constants.ZulFiles.LOGIN_SUCCESS_REDIRECT);
        }
        if (pagename == null) {
            LOGGER.info("No current pagename, redirecting to home...");
            Executions.getCurrent().sendRedirect(Constants.ZulFiles.HOME);
        }
    }

    /**
     *
     * @param parsingError .
     * @return boolean
     * @throws Exception .
     */
    public final boolean doCatch(Throwable parsingError) throws Exception {
        return false;
    }

    /**
     *
     * @param page .
     * @throws Exception .
     */
    public void doAfterCompose(Page page) throws Exception { }

    /**
     *
     * @throws Exception .
     */
    public void doFinally() throws Exception { }

}
