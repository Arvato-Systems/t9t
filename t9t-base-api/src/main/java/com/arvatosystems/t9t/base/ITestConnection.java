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
package com.arvatosystems.t9t.base;

import java.util.UUID;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

/** Common methods for embedded and remote tests. */
public interface ITestConnection {
    void switchUser(UUID newApiKey)                                 throws Exception;   // authenticate a new user by API key
    void switchUser(String userId, String password)                 throws Exception;
    void switchTenant(String newTenantId, int expectedCode)         throws Exception;   // switch to a new tenant
    void switchLanguage(String newLanguage)                         throws Exception;   // switch to a different language
    BonaPortable doIO(BonaPortable rp)                              throws Exception;   // send any request
    ServiceResponse srIO(RequestParameters rp)                      throws Exception;   // send any request and perform a type check
    ServiceResponse okIO(RequestParameters rp)                      throws Exception;   // send any request and check result for OK, throws an Exception if not OK
    <T extends ServiceResponse> T typeIO(RequestParameters rp, Class<T> responseClass) throws Exception; // send any request and check result for OK, throws an Exception if not OK or not of the expected class
    void errIO(RequestParameters rp, int errorCode)                 throws Exception;   // send any request and check result for a specific error code, throws an Exception if another result was received
    AuthenticationResponse auth(AuthenticationParameters params)    throws Exception;   // authenticate, throw an Exception if it fails, store the credentials for subsequent requests if everything OK
    AuthenticationResponse auth(String myUserId, String myPassword) throws Exception;   // authenticate as in the general case, shorthand for username / password
    AuthenticationResponse auth(UUID apiKey)                        throws Exception;   // authenticate as in the general case, shorthand for API key
    void logout()                                                   throws Exception;   // terminate communication (close connection for remote tests)
    // methods not required for normal operation, but for testing of specific scenarios
    void setAuthentication(String header);                                              // set a faked Authorization header (only for remote tests)

    AuthenticationResponse changePassword(String myUserId, String myPassword, String newPassword) throws Exception;

    String getLastJwt();
    JwtInfo getLastJwtInfo();
}
