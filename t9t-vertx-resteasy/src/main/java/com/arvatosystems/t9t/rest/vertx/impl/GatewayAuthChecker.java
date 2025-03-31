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
package com.arvatosystems.t9t.rest.vertx.impl;

import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.rest.services.IGatewayAuthChecker;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class GatewayAuthChecker implements IGatewayAuthChecker {

    protected final ICachingAuthenticationProcessor authenticationProcessor = Jdp.getRequired(ICachingAuthenticationProcessor.class);

    @Override
    public boolean isValidAuth(final String authHeader, final AuthenticationParameters authParams) {
        return authenticationProcessor.getCachedJwt(authHeader) != null;
    }
}
