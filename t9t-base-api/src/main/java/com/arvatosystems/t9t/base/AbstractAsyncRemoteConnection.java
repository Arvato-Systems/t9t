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
package com.arvatosystems.t9t.base;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;

import de.jpaw.util.ExceptionUtil;

public abstract class AbstractAsyncRemoteConnection implements IRemoteConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsyncRemoteConnection.class);

    @Override
    public ServiceResponse execute(final String authenticationHeader, final RequestParameters rp) {
        try {
            return executeAsync(authenticationHeader, rp).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Remote invocation exception: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.BAD_REMOTE_RESPONSE, e.getClass().getSimpleName());
        }
    }

    @Override
    public ServiceResponse executeAuthenticationRequest(final AuthenticationRequest rp) {
        try {
            return executeAuthenticationAsync(rp).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Remote invocation exception: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.BAD_REMOTE_RESPONSE, e.getClass().getSimpleName());
        }
    }
}
