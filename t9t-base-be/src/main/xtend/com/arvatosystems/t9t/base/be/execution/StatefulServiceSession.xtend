/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
import java.util.concurrent.atomic.AtomicReference
import de.jpaw.annotations.AddLogger

/** This class implements a stateful session. It connects to a stateless backend.
 * The class is not multithreading-capable, except for execute requests - a separate client is required per session.
 */
@AddLogger
@Dependent
class StatefulServiceSession implements IStatefulServiceSession {
    val AtomicReference<AuthInfo> authInfo = new AtomicReference(null)

    @Inject IRequestProcessor processor
    @Inject IAuthenticate authBackend

    def private authenticate(SessionParameters sessionParameters, AuthenticationParameters authenticationParameters) {
        val r = authBackend.login(new AuthenticationRequest => [
            it.sessionParameters        = sessionParameters
            it.authenticationParameters = authenticationParameters
        ])
        if (r !== null && r.returnCode == 0) {
            // success
            return new AuthInfo(sessionParameters, authenticationParameters, r.jwtInfo, r.encodedJwt)
        }
        // failure
        return null
    }

    override void open(SessionParameters sessionParameters, AuthenticationParameters authenticationParameters) {
        authenticationParameters.freeze
        val info = authenticate(sessionParameters, authenticationParameters)
        authInfo.set(info)
        if (info === null) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED)
        }
    }

    override boolean isOpen() {
        return authInfo.get !== null
    }

    override void close() {
        authInfo.set(null)
    }

    override ServiceResponse execute(RequestParameters rp) {
        // regular request: JWT required!
        val info = authInfo.get()
        if (info === null)
            throw new T9tException(T9tException.NOT_AUTHENTICATED)
        // OK, authenticated!
        val resp = processor.execute(null, rp, info.jwtInfo, info.encodedJwt, false)
        if (resp.returnCode == 0) {
            if (resp instanceof AuthenticationResponse) {
                // update JWT
                if (rp instanceof AuthenticationRequest) {
                    authInfo.set(new AuthInfo(rp.sessionParameters, rp.authenticationParameters, resp.jwtInfo, resp.encodedJwt))
                } else {
                    // keep the old ones
                    authInfo.set(new AuthInfo(info.sessionParameters, info.authenticationParameters, resp.jwtInfo, resp.encodedJwt))
                }
            }
        } else {
            // any issue
            if (resp.returnCode == T9tException.JWT_EXPIRED) {
                // expired: must do a retry
                LOGGER.info("JWT expired, performing reauth")
                val newInfo = authenticate(info.sessionParameters, info.authenticationParameters)
                if (newInfo === null) {
                    throw new T9tException(T9tException.NOT_AUTHENTICATED)
                }
                authInfo.set(newInfo)
                return processor.execute(null, rp, newInfo.jwtInfo, newInfo.encodedJwt, false)
            }
        }
        return resp
    }

    override Instant authenticatedUntil() {
        return authInfo.get()?.jwtInfo?.expiresAt
    }

    override String getTenantId() {
        return authInfo.get()?.jwtInfo?.tenantId;
    }
}
