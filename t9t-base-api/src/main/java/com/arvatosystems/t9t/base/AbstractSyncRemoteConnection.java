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

import java.util.concurrent.CompletableFuture;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;

public abstract class AbstractSyncRemoteConnection implements IRemoteConnection {

    @Override
    public CompletableFuture<ServiceResponse> executeAsync(final String authenticationHeader, final RequestParameters rp) {
        return CompletableFuture.completedFuture(execute(authenticationHeader, rp));
    }

    @Override
    public CompletableFuture<ServiceResponse> executeAuthenticationAsync(final AuthenticationRequest rp) {
        return CompletableFuture.completedFuture(executeAuthenticationRequest(rp));
    }
}
