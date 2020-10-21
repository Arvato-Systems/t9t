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
package com.arvatosystems.t9t.tfi.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;

// UNUSED
@Deprecated
public class ApplicationScope {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationScope.class);

    private static final String APP_SCOPE = "APP_SCOPE";
    private static Object lock = new Object();

    private ApplicationScope() {

    }

    public static ApplicationScope get() {
        final Execution exec = Executions.getCurrent();
        if (exec != null) {
            final WebApp app = exec.getDesktop().getWebApp();
            ApplicationScope appScope = (ApplicationScope) app.getAttribute(APP_SCOPE);
            if (appScope == null) {
                synchronized (lock) {
                    if (appScope == null) {
                        app.setAttribute(APP_SCOPE, appScope = new ApplicationScope());
                    }
                }
            }
            return appScope;
        }
        throw new IllegalStateException("Unable to get/create application scope");
    }
}
