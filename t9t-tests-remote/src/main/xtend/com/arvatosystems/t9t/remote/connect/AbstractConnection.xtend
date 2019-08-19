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
package com.arvatosystems.t9t.remote.connect

import com.arvatosystems.t9t.authc.api.SwitchLanguageRequest
import com.arvatosystems.t9t.authc.api.SwitchTenantRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.auth.PasswordAuthentication
import com.arvatosystems.t9t.base.types.AuthenticationParameters
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.util.ApplicationException
import java.util.UUID

@AddLogger
abstract class AbstractConnection extends ConnectionDefaults implements ITestConnection {

    // cache the last JWT / JwtInfo record
    String lastJwt = null;
    JwtInfo lastJwtInfo = null;


    // run a request and expect that it returns a SericeResponse
    override final ServiceResponse srIO(RequestParameters rp) {
        val resp = doIO(rp)
        if (resp === null) {
            LOGGER.error("Request {} returned null", rp.ret$PQON)
        } else {
            if (resp instanceof ServiceResponse) {
                return resp
            } else {
                LOGGER.error("Request {} returned an object of class {}", rp.ret$PQON, resp.class.canonicalName)
            }
        }
        throw new RuntimeException("Abnormal end")
    }

    // run a request and expect that is completes successfully
    override final ServiceResponse okIO(RequestParameters rp) {
        val resp = srIO(rp)
        if (ApplicationException.isOk(resp.returnCode))
            return resp
        LOGGER.error("Request {} returned an error: code {}: {} ({})", rp.ret$PQON, resp.returnCode, resp.errorDetails, resp.errorMessage)
        throw new RuntimeException("Abnormal end")
    }

    // run a request and expect that is completes successfully
    override final <T extends ServiceResponse> T typeIO(RequestParameters rp, Class<T> responseClass) {
        val resp = okIO(rp)
        if (responseClass.isAssignableFrom(resp.class))
            return resp as T
        LOGGER.error("Request {} returned class {}, but expected {}", rp.ret$PQON, resp.class.canonicalName, responseClass.canonicalName)
        throw new RuntimeException("Abnormal end")
    }

    // run a request and expect a specific error code
    override final void errIO(RequestParameters rp, int errorCode) {
        val resp = srIO(rp)
        if (resp.returnCode != errorCode) {
            LOGGER.error("Request {} expected {}, but returned {}: {} ({})", rp.ret$PQON, errorCode, resp.returnCode, resp.errorDetails, resp.errorMessage)
            throw new RuntimeException("Abnormal end")
        } else {
            LOGGER.info("Expected / wanted error {} and got it.", errorCode);
        }
    }

    override final AuthenticationResponse auth(AuthenticationParameters params) {
        // before a new auth attempt, clear the last
        lastJwtInfo = null
        lastJwt     = null
        val authResult = typeIO(new AuthenticationRequest => [
            authenticationParameters    = params
            sessionParameters           = SESSION_PARAMETERS
            validate
        ], AuthenticationResponse)
        lastJwtInfo = authResult.jwtInfo
        lastJwt     = authResult.encodedJwt
        return authResult;
    }

    override final AuthenticationResponse auth(String myUserId, String myPassword) {
        return auth(new PasswordAuthentication => [
                userId                  = myUserId
                password                = myPassword
            ]
        )
    }

    override final AuthenticationResponse changePassword(String myUserId, String myPassword, String newPassword) {
        return auth (new PasswordAuthentication => [
            userId                  = myUserId
            password                = myPassword
            it.newPassword          = newPassword
        ])
    }

    override final AuthenticationResponse auth(UUID apiKey) {
        return auth(new ApiKeyAuthentication(apiKey))
    }

    override void switchTenant(String newTenantId, int expectedCode) {
        val rq = new SwitchTenantRequest(newTenantId)
        if (ApplicationException.isOk(expectedCode)) {
            val authResult = typeIO(rq, AuthenticationResponse)
            lastJwtInfo    = authResult.jwtInfo
            lastJwt        = authResult.encodedJwt
            setAuthentication("Bearer " + authResult.encodedJwt)  // the encoded token
        } else {
            errIO(rq, expectedCode)
        }
    }

    override switchLanguage(String newLanguage) throws Exception {
        val rq = new SwitchLanguageRequest(newLanguage)
        val authResult = typeIO(rq, AuthenticationResponse)
        lastJwtInfo    = authResult.jwtInfo
        lastJwt        = authResult.encodedJwt
        setAuthentication("Bearer " + authResult.encodedJwt)  // the encoded token
    }

    override getLastJwt() {
        return lastJwt
    }

    override getLastJwtInfo() {
        return lastJwtInfo
    }
}
