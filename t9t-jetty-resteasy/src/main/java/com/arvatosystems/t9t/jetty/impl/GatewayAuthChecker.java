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
package com.arvatosystems.t9t.jetty.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.rest.services.IGatewayAuthChecker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
public class GatewayAuthChecker implements IGatewayAuthChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayAuthChecker.class);

    protected final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);
    protected final Cache<AuthenticationParameters, Boolean> authenticationCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).<AuthenticationParameters, Boolean>build();

    @Override
    public boolean isValidAuth(final String authHeader, final AuthenticationParameters authParams) {
        // first, check if our cache knows about this authentication
        authParams.freeze();
        final Boolean isAuthenticated = authenticationCache.getIfPresent(authParams);
        if (isAuthenticated != null) {
            return isAuthenticated.booleanValue();
        }
        LOGGER.info("Checking auth for backend");
        final AuthenticationRequest authRq = new AuthenticationRequest();
        final SessionParameters session = new SessionParameters();
        session.setDataUri("GWAC");  // short for gateway auth check
        authRq.setAuthenticationParameters(authParams);
        authRq.setSessionParameters(session);
        final ServiceResponse authResponse = connection.executeAuthenticationRequest(authRq);
        final Boolean isOk = ApplicationException.isOk(authResponse.getReturnCode());
        authenticationCache.put(authParams, isOk);
        LOGGER.info("Checked auth for backend: {}", isOk);
        return isOk;
    }
}
