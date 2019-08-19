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
package com.arvatosystems.t9t.server.services;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.server.InternalHeaderParameters;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

/** Interface between a specific server implementation (for example servlet, netty, or vert.x based) and the execution backend.
 * Can also be used for a client, in which case the implementation will send the request to the remote server.
 * This is a specialized & streamlined version of bonaparte.api.auth.IRequestProcessor, without the support methods required by generics. */

public interface IRequestProcessor {
    /** Execute a request. For server implementations, authentication has been performed and jwtInfo is the decoded user / session information,
     * and encodedJwt provides the signed token in case nested executions have to be performed.
     * Implementations have to catch all exceptions and populate a return code and error details in case anything fails.
     * skipAuthorization can be set to true, if the caller is known to originate from an internal context and has been pre-authorized (i.e. for sub services).
     */
    ServiceResponse execute(ServiceRequestHeader hdr, RequestParameters rq, JwtInfo jwtInfo, String encodedJwt, boolean skipAuthorization);

    <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestParameters params, InternalHeaderParameters ihdr, Class<T> requiredType, boolean skipAuthorization);
}
