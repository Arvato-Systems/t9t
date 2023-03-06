/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.rest.services.IGatewayAuthChecker;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
public class GatewayAuthChecker implements IGatewayAuthChecker {

    protected final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);

    @Override
    public boolean isValidAuth(final String authHeader, final AuthenticationParameters authParams) {
        final AuthenticationRequest authRq = new AuthenticationRequest();
        authRq.setAuthenticationParameters(authParams);
        final ServiceResponse authResponse = connection.executeAuthenticationRequest(authRq);
        return ApplicationException.isOk(authResponse.getReturnCode());
    }
}
