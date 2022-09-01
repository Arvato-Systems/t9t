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

/**
 * Defines an interface to a remote t9t based service.
 * Instances operate with a fixed endpoint, which is either passed by the constructor,
 * or, if instantiated as a @Singleton, read from environment variables or system properties.
 * t9t.port, t9t.host, t9t.rpcpath, t9t.authpath
 *
 * An appropriate implementation of it is selected by adding the respective JAR.
 * The currently preferred implementations both use the new JDK 11 client, either pooled or directly.
 * Request serialization is always done via "compact bonaparte".
 */
public interface IRemoteConnection {
    /** execute a single (regular) request for an authenticated context. */
    ServiceResponse execute(String authenticationHeader, RequestParameters rp);

    /** authenticate. */
    ServiceResponse executeAuthenticationRequest(AuthenticationRequest rp);

    /** execute a single (regular) request for an authenticated context, asynchronously. */
    CompletableFuture<ServiceResponse> executeAsync(String authenticationHeader, RequestParameters rp);

    /** authenticate. */
    CompletableFuture<ServiceResponse> executeAuthenticationAsync(AuthenticationRequest rp);
}
