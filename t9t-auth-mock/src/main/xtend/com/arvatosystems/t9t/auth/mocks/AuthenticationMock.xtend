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
package com.arvatosystems.t9t.auth.mocks

import com.arvatosystems.t9t.auth.services.IAuthenticator
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication
import com.arvatosystems.t9t.base.auth.AuthX500DistinguishedName
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.auth.PasswordAuthentication
import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.server.services.IAuthenticate
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.ObjectValidationException
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.dp.Fallback
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.util.ApplicationException
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

@Fallback
@Singleton
@AddLogger
class AuthenticationMock implements IAuthenticate {

    @Inject IAuthenticator authenticator

    override login(AuthenticationRequest rq) {
        val ap = rq.authenticationParameters
        if (ap === null) {
            LOGGER.info("Authentication without parameters")
            throw new ObjectValidationException(ObjectValidationException.MAY_NOT_BE_BLANK)
        }
        LOGGER.info("Authentication for method {}", ap.ret$PQON)

        val jwt = auth(ap)      // dispatch

        if (jwt === null)
            throw new ApplicationException(ApplicationException.CL_DENIED * 100000000 + 1)


        return new AuthenticationResponse => [
            jwtInfo     = jwt
            encodedJwt  = authenticator.doSign(jwt)
            tenantName  = "ACME Corp."
            tenantId    = "ACME"
            processRef  = 0L
        ]
    }

    public static final String DEMO_API_KEY     = "fec1e81e-0ca7-4709-b865-7150975d2c78";
    private static final UUID DEMO_API_KEY_UUID = UUID.fromString(DEMO_API_KEY);
    private static final JwtInfo DEMO_JWT_INFO  = new JwtInfo => [
        userId          = "john"
        userRef         = 93847593L
        tenantId        = "ACME"
        tenantRef       = 98347569L
        name            = "John E. Smith"
        locale          = "en"
        zoneinfo        = "UTC"
        permissionsMin  = Permissionset.ofTokens(OperationType.EXECUTE, OperationType.READ, OperationType.CREATE)
        permissionsMax  = Permissionset.ofTokens(OperationType.EXECUTE, OperationType.READ, OperationType.CREATE, OperationType.DELETE, OperationType.UPDATE)
        freeze
    ]
    private static final AtomicLong sessionCounter = new AtomicLong(77000L)

    def protected dispatch auth(ApiKeyAuthentication ap) {
        if (ap.apiKey != DEMO_API_KEY_UUID)
            return null
        Thread.sleep(2_000)  // test for execute blocking!
        val myInfo = DEMO_JWT_INFO.ret$MutableClone(false, false)
        myInfo.sessionRef = sessionCounter.incrementAndGet
        myInfo.jsonTokenIdentifier = myInfo.sessionRef.toString
        return myInfo
    }

    def protected dispatch auth(PasswordAuthentication ap) {
        if ("john" != ap.userId || "secret" != ap.password)
            return null;
        val myInfo = DEMO_JWT_INFO.ret$MutableClone(false, false)
        myInfo.sessionRef = sessionCounter.incrementAndGet
        myInfo.jsonTokenIdentifier = myInfo.sessionRef.toString
        return myInfo
    }

    def protected dispatch auth(AuthX500DistinguishedName dn) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }

    def protected dispatch auth(AuthenticationParameters unknown) {
        throw new UnsupportedOperationException("Unsupported authentication parameters")
    }
}
