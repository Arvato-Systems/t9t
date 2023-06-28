/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
    /**
     * Authenticates a new user by API key.
     *
     * @param newApiKey         the API key for authentication
     * @throws T9tException     if the authentication was not successful
     */
    void switchUser(UUID newApiKey);

    /**
     * Switches connection to use another user, using username / password authentication.
     *
     * @param userId            the user ID to switch to
     * @param password          the password of the new user
     * @throws T9tException     if the authentication was not successful
     * */
    void switchUser(String userId, String password);

    /**
     * Switches connection to use another tenant (given the user has permission to it).
     *
     * @param newTenantId       the ID of the new tenant
     * @param expectedCode      the expected code (OK or denied)
     * @throws T9tException     if the authentication returned a different than the expected result
     */
    void switchTenant(String newTenantId, int expectedCode);

    /**
     * Switches connection to use another language.
     *
     * @param newLanguage       the BCP 47 language tag of the new target language
     */
    void switchLanguage(String newLanguage);

    /**
     * Performs an invocation of the backend.
     * This is a technical internal method and not used by most tests.
     *
     * @param rp                the object to send (which is usually a subclass of <code>RequestParameters</code>)
     * @return                  the result of the request execution
     */
    BonaPortable doIO(BonaPortable rp);

    /**
     * Performs an invocation of the backend for request.
     * This method also performs a check for the general type of ServicerResponse.
     *
     * @param rp                the request parameters
     * @return                  the result of the request execution
     */
    ServiceResponse srIO(RequestParameters rp);

    /**
     * Performs an invocation of the backend for request.
     * This method also performs a check for the general type of ServiceResponse.
     * It also checks for an OK return code and throws an exception if the returnCode does not belong to CL_OK.
     *
     * @param rp                the request parameters
     * @return                  the result of the request execution
     */
    ServiceResponse okIO(RequestParameters rp);

    /**
     * Performs an invocation of the backend for request.
     * This method also performs a check for the specific type of ServiceResponse. IT always expects an OK result.
     *
     * @param rp                the request parameters
     * @return                  the result of the request execution
     * @throws                  a T9tException if the return code was not successful or the result returned was not of the expected type
     */
    <T extends ServiceResponse> T typeIO(RequestParameters rp, Class<T> responseClass);


    /**
     * Performs an invocation of the backend for request.
     * This method also performs a check for a specific error code in the field <code>returnCode</code> of ServiceResponse.
     *
     * @param rp                the request parameters
     * @param errorCode         the expected error return code
     * @throws                  a T9tException if the return code did not match the expected error code
     */
    void errIO(RequestParameters rp, int errorCode);

    // authenticate, throw an Exception if it fails, store the credentials for subsequent requests if everything OK
    AuthenticationResponse auth(AuthenticationParameters params);

    // authenticate as in the general case, shorthand for username / password
    AuthenticationResponse auth(String myUserId, String myPassword);

    // authenticate as in the general case, shorthand for API key
    AuthenticationResponse auth(UUID apiKey);

    // terminate communication (close connection for remote tests)
    void logout();

    /**
     * Set a specific Authorization header.
     * This method only exists to support remote tests, required to be able to test incorrect authentications.
     *
     * @param header            the header to send as HTTP request parameter "Authorization"
     */
    void setAuthentication(String header);

    /**
     * Changes the password for the specified user.
     * The implementation may perform checks on password requirements such as minimum length, character set, matches to previous passwords etc.
     * and reject the new password if it does not comply with the specified requirements. The change will also be rejected if the current user
     * is not an administrator or if the current password is not correct.
     *
     * @param myUserId          the user ID to change the password for
     * @param myPassword        the current password of the user
     * @param newPassword       the intended new password for the user
     * @return                  the result of the request execution
     */
    AuthenticationResponse changePassword(String myUserId, String myPassword, String newPassword);

    /**
     * Retrieves the JWT as encoded string.
     *
     * @return                  the Base64 encoded JWT
     */
    String getLastJwt();

    /**
     * Retrieves the JWT as decoded data structure.
     *
     * @return                  the JWT parsed into an instance of class <code>JwtInfo</code>
     */
    JwtInfo getLastJwtInfo();
}
