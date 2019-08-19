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
package com.arvatosystems.t9t.embedded.connect

import com.arvatosystems.t9t.authc.api.SwitchTenantRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.auth.PasswordAuthentication
import com.arvatosystems.t9t.base.types.AuthenticationJwt
import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.base.types.SessionParameters
import com.arvatosystems.t9t.server.services.IAuthenticate
import com.arvatosystems.t9t.server.services.IRequestProcessor
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Inject
import de.jpaw.util.ApplicationException
import java.util.UUID
import de.jpaw.bonaparte.core.BonaPortable
import com.arvatosystems.t9t.authc.api.SwitchLanguageRequest

@AddLogger
abstract class AbstractConnection implements ITestConnection {

    public static final String INITIAL_TENANT_ID    = "@";
    public static final String INITIAL_USER_ID      = "admin";
    public static final String INITIAL_PASSWORD     = "changeMe";

    static final SessionParameters SESSION_PARAMETERS = new SessionParameters => [
        dataUri                 = System.getProperty("user.name")   // optional identifier of the local host
        locale                  = "en-US"                           // BCP 47 language tag (ISO630 language code)
        userAgent               = "t9t-local-tests"                 // any string to identify the client
        zoneinfo                = "Europe/Berlin"                   // IANA tz identifier
        validate
        freeze
    ]

    static final Object DUMMY_INIT_1 = {
        System.setProperty("org.jboss.logging.provider", "slf4j");              // configure hibernate to use slf4j
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
        return null
    }

    @Inject IRequestProcessor                       requestProcessor        // processor for authenticated requests
    @Inject IAuthenticate                           authenticate            // processor for logins
    @Inject IUnauthenticatedServiceRequestExecutor  serviceRequestExecutor  // processor for unauthenticated requests

    var AuthenticationParameters authentication = null      // credentials for unauthenticated requests
    var String encodedJwt = null                            // credentials part 1 (authentication already validated)
    var JwtInfo jwtInfo = null                              // credentials part 2 (authentication already validated)
    var boolean userAuthenticatedCredentials = true         // which one of the above to use - update as desired
    var boolean skipAuthorization = true;                   // flip to false if authorization should be tested

    override void switchUser(UUID newApiKey) {
        auth(newApiKey)
    }

    override void switchUser(String userId, String password) {
        auth(userId, password)
    }

    override void switchTenant(String newTenantId, int expectedCode) {
        val rq = new SwitchTenantRequest(newTenantId)
        if (ApplicationException.isOk(expectedCode)) {
            val authResult  = typeIO(rq, AuthenticationResponse)
            encodedJwt      = authResult.encodedJwt
            jwtInfo         = authResult.jwtInfo
            authentication  = new AuthenticationJwt(authResult.encodedJwt)  // may expire, but we have no other choice here
        } else {
            errIO(rq, expectedCode)
        }
    }

    // just a proxy
    def final ServiceResponse doIO(RequestParameters rp) {
        if (userAuthenticatedCredentials) {
            return requestProcessor.execute(null, rp, jwtInfo, encodedJwt, skipAuthorization)
        } else {
            return serviceRequestExecutor.execute(new ServiceRequest(null, rp, authentication))
        }
    }

    // run a request and expect that it returns a SericeResponse
    override final ServiceResponse srIO(RequestParameters rp) {
        val resp = doIO(rp)
        if (resp === null) {
            LOGGER.error("Request {} returned null", rp.ret$PQON)
        } else {
            return resp
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
        encodedJwt      = null
        jwtInfo         = null
        val authResult = authenticate.login(new AuthenticationRequest => [
            authenticationParameters    = params
            sessionParameters           = SESSION_PARAMETERS
            validate
        ])
        encodedJwt      = authResult.encodedJwt
        jwtInfo         = authResult.jwtInfo
        authentication  = params
        return authResult
    }

    override final AuthenticationResponse auth(String myUserId, String myPassword) {
        return auth(new PasswordAuthentication => [
            userId                  = myUserId
            password                = myPassword
        ])
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

    override void logout() {
    }

    // methods not required for normal operation, but for testing of specific scenarios
    override void setAuthentication(String header) {
        throw new UnsupportedOperationException     // not available for local tests
    }

    override doIO(BonaPortable rp) throws Exception {
        if (rp instanceof RequestParameters)
            return doIO(rp)
        else
            throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }

    override switchLanguage(String newLanguage) throws Exception {
        val rq = new SwitchLanguageRequest(newLanguage)
        val authResult = typeIO(rq, AuthenticationResponse)
        jwtInfo        = authResult.jwtInfo
        encodedJwt     = authResult.encodedJwt
        setAuthentication("Bearer " + authResult.encodedJwt)  // the encoded token
    }

    override getLastJwt() {
        return encodedJwt
    }

    override getLastJwtInfo() {
        return jwtInfo
    }
}
