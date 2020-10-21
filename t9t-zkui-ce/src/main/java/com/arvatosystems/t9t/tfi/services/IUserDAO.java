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
package com.arvatosystems.t9t.tfi.services;

import java.util.List;

import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PermissionEntry;

public interface IUserDAO {

    /**
     * Authenticates the user.  Clears permissions.
     * @param username Login name.
     * @param pwd password.
     * @param newPassword if resetting the password , give the new password.
     * @param authenticationType like LDAP,SSO,Database...
     * @return AuthenticationResponse User and User details.
     */
    public AuthenticationResponse getAuthenticationResponse( String username, String pwd) throws ReturnCodeException;

    public List<PermissionEntry> getPermissions() throws ReturnCodeException;

    /**
     * change password.
     * <pre>
     *     class ChangePasswordRequest {
     *         required Binary(64) newPassword; // The new sha-512 hashed password, salted by username
     *         required Binary(64) oldPassword; // The old sha-512 hashed password, salted by username
     *     }
     * </pre>
     */
    public void changePassword(String oldPassword, String newPassword) throws ReturnCodeException;

    /**
     * reset password.
     *
     * <pre>
     * The request returns only the response Header, with a return code 0 for success or > 0 for errors.
     * User needs to be super user in order to perform this operation (i.e. tenant ID = @)
     *     class ResetPasswordRequest {
     *         required extUserId userId; // computer generated short user Id (visible externally)
     *         required Ascii(255) emailAddress; // email address of user, if known
     *     }
     * </pre>
     */
    public void resetPassword(String userId, String emailAddress) throws ReturnCodeException;

    /** Once a tenant has been selected in the second screen, an additional backend call is now required to update the JWT
     * @param tenantId
     * @return AuthenticationResponse response
     * @throws ReturnCodeException
     */
    public AuthenticationResponse switchTenant(String tenantId) throws ReturnCodeException;

    /** Once a language is changed, an additional backend call is now required to update the JWT.
     * This call can also be used to refresh the JWT.
     * @param tenantId
     * @return AuthenticationResponse response
     * @throws ReturnCodeException
     */
    public AuthenticationResponse switchLanguage(String language) throws ReturnCodeException;

}
