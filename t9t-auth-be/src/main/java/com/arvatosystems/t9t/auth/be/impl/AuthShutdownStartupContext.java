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

import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upon server start, there is a RequestContext created for the initial thread.
 * This context is active after completion of step 50005 (AuthCreateStartupContext) and is terminated by this step
 * in step 99995.
 */
@Startup(99995)
public class AuthShutdownStartupContext implements StartupOnly {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthShutdownStartupContext.class);
    private final RequestContextScope ctxScope = Jdp.getRequired(RequestContextScope.class);

    @Override
    public void onStartup() {
        LOGGER.info("Auth module startup - terminating launch context");

        final RequestContext ctx = ctxScope.get();
        ctxScope.close();
        try {
            ctx.close();
        } catch (Exception e) {
            LOGGER.error("Auth module startup - error while terminating launch context");
        }
    }
}
