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
package com.arvatosystems.t9t.base.be.execution

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.base.types.SessionParameters
import com.arvatosystems.t9t.server.services.IAuthenticate
import com.arvatosystems.t9t.server.services.IRequestProcessor
import com.arvatosystems.t9t.server.services.IStatefulServiceSession
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Dependent
import de.jpaw.dp.Inject
import org.joda.time.Instant

/** This class implements a stateful session. It connects to a stateless backend.
 * The class is not multithreading-capable, except for execute requests - a separate client is required per session.
 */
@Dependent
class StatefulServiceSession implements IStatefulServiceSession {
    String encodedJwt
    SessionParameters sessionParameters
    JwtInfo jwtInfo

    @Inject IRequestProcessor processor
    @Inject IAuthenticate authBackend

    override void open(SessionParameters sessionParameters, AuthenticationParameters authenticationParameters) {
        this.sessionParameters = sessionParameters  // store for future enrichment of logins
        jwtInfo     = null;
        encodedJwt  = null;
        if (authenticationParameters !== null) {
            // attempt to authenticate
            val r = authBackend.login(new AuthenticationRequest => [
                it.sessionParameters        = sessionParameters
                it.authenticationParameters = authenticationParameters
            ])
            if (r !== null && r.returnCode == 0) {
                // success
                jwtInfo         = r.jwtInfo
                encodedJwt      = r.encodedJwt
            } else {
                throw new T9tException(T9tException.NOT_AUTHENTICATED)
            }
        }
    }

    override boolean isOpen() {
        return encodedJwt !== null
    }

    override void close() {
        encodedJwt = null
        jwtInfo = null
    }

    override ServiceResponse execute(RequestParameters rp) {
        // regular request: JWT required!
        if (!isOpen)
            throw new T9tException(T9tException.NOT_AUTHENTICATED)
        // OK, authenticated!
        val resp = processor.execute(null, rp, jwtInfo, encodedJwt, false)
        if (resp.returnCode == 0) {
            if (resp instanceof AuthenticationResponse) {
                // update JWT
                jwtInfo         = resp.jwtInfo
                encodedJwt      = resp.encodedJwt
            }
        }
        return resp
    }

    override Instant authenticatedUntil() {
        return jwtInfo?.expiresAt
    }

    override String getTenantId() {
        return jwtInfo?.tenantId;
    }

}
