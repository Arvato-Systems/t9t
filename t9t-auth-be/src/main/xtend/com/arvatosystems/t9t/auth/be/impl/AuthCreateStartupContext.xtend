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
package com.arvatosystems.t9t.auth.be.impl

import com.arvatosystems.t9t.auth.services.IAuthenticator
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.be.execution.RequestContextScope
import com.arvatosystems.t9t.base.services.ICustomization
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.server.InternalHeaderParameters
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Startup
import de.jpaw.dp.StartupOnly

@Startup(50005)
@AddLogger
class AuthCreateStartupContext implements StartupOnly, T9tConstants {
//    private static final long ONE_DAY_IN_MS = 1000L * 24L * 3600L;

    @Inject IAuthenticator              authenticator
    @Inject ICustomization              customizationProvider
    @Inject RequestContextScope         ctxScope

    override onStartup() {
        LOGGER.info("Auth module startup - creating launch context")

        val jwt = T9tDefaultContext.STARTUP_JWT
        val encoded = authenticator.doSign(jwt, 10L * 60) // create a JWT for 10 minutes - should be long enough to start
        val ihdr = new InternalHeaderParameters(encoded, jwt, 1L, "en", jwt.issuedAt, null, "t9t.StartupRequest")
        val ctx = new RequestContext(ihdr, customizationProvider) // create a context
        ctxScope.set(ctx)
    }
}


@Startup(99995)
@AddLogger
class AuthShutdownStartupContext implements StartupOnly {

    @Inject RequestContextScope         ctxScope

    override onStartup() {
        LOGGER.info("Auth module startup - terminating launch context")

        val ctx = ctxScope.get
        ctxScope.close
        ctx.close
    }
}
