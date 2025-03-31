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

import com.arvatosystems.t9t.auth.services.IAuthenticator;
import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.services.ICustomization;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upon server start, there is a RequestContext created for the initial thread.
 * This context is active after completion of step 50005 (this step) and is terminated by AuthShutdownStartupContext
 * in step 99995.
 */
@Startup(50005)
public class AuthCreateStartupContext implements StartupOnly {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthCreateStartupContext.class);

    private final IAuthenticator authenticator = Jdp.getRequired(IAuthenticator.class);
    private final ICustomization customizationProvider = Jdp.getRequired(ICustomization.class);
    private final RequestContextScope ctxScope = Jdp.getRequired(RequestContextScope.class);

    @Override
    public void onStartup() {
        LOGGER.info("Auth module startup - creating launch context");

        final JwtInfo jwt = T9tDefaultContext.STARTUP_JWT;
        final String encoded = authenticator.doSign(jwt, 10L * 60); // create a JWT for 10 minutes - should be long enough to start
        final InternalHeaderParameters ihdr = new InternalHeaderParameters(encoded, jwt, 1L, "en", jwt.getIssuedAt(), null, "t9t.StartupRequest");
        final RequestContext ctx = new RequestContext(ihdr, customizationProvider); // create a context
        ctxScope.set(ctx);
    }
}
