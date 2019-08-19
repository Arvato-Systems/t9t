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
package com.arvatosystems.t9t.client.connection;

import java.util.List;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;

public interface IRemoteConnection {
    /** execute a single (regular) request for an authenticated context. */
    ServiceResponse execute(String authenticationHeader, RequestParameters rp);

    /** execute one or more (regular) requests for an authenticated context.
     * If the list size is more than one, a batch request will be constructed. */
    ServiceResponse execute(String authenticationHeader, List<RequestParameters> rpList);

    /** authenticate. */
    ServiceResponse executeAuthenticationRequest(AuthenticationRequest rp);
}
