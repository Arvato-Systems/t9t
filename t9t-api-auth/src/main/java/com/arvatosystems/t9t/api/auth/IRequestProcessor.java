/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.api.auth;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

/** Interface between a specific server implementation (for example servlet, netty, or vert.x based) and the execution backend.
 * Can also be used for a client, in which case the implementation will send the request to the remote server. */
public interface IRequestProcessor<RQ extends BonaPortable, RS extends BonaPortable> {
    ObjectReference getRequestRef();
    ObjectReference getResponseRef();

    /** execute a request. For server implementations, authentication has been performed,jwtInfo is the decoded user / session information,
     * and encodedJwt provides the signed token in case nested executions have to be performed.
     * Implementations have to catch all exceptions and populate a return code and error details in case anything fails.
     */
    RS execute(RQ rq, JwtInfo jwtInfo, String encodedJwt);

    int getReturnCode(RS rs);
    String getErrorDetails(RS rs);
}
