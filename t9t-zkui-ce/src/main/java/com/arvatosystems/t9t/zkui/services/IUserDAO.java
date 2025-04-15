/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.services;

import java.util.List;
import java.util.UUID;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.request.GetPasswordChangeRequirementsResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;

import jakarta.annotation.Nonnull;

public interface IUserDAO {

    /**
     * Authenticates the user by username / password.  Clears permissions.
     * @param username Login name.
     * @param pwd password.
     * @return AuthenticationResponse user and user details.
     */
    AuthenticationResponse getUserPwAuthenticationResponse(@Nonnull String username, @Nonnull String pwd) throws ReturnCodeException;

    /**
     * Authenticates by API-key (password forgotten).  Clears permissions.
     * @param apiKey the API key for the "password forgotten" request.
     * @return AuthenticationResponse user and user details.
     */
    AuthenticationResponse getApiKeyAuthenticationResponse(@Nonnull UUID apiKey) throws ReturnCodeException;

    /**
     * Authenticates the user via external access token
     * @param accessToken   token received from external identity provider.
     * @param username preliminary guess of user identification
     * @return AuthenticationResponse user and user details.
     */
    AuthenticationResponse getExternalTokenAuthenticationResponse(@Nonnull String accessToken, @Nonnull String username) throws ReturnCodeException;

    List<PermissionEntry> getPermissions() throws ReturnCodeException;

    /**
     * change password.
     * <pre>
     *     class ChangePasswordRequest {
     *         required Binary(64) newPassword; // The new sha-512 hashed password, salted by username
     *         required Binary(64) oldPassword; // The old sha-512 hashed password, salted by username
     *     }
     * </pre>
     */
    void changePassword(@Nonnull String oldPassword, @Nonnull String newPassword) throws ReturnCodeException;

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
    void resetPassword(@Nonnull String userId, @Nonnull String emailAddress) throws ReturnCodeException;

    /** Once a tenant has been selected in the second screen, an additional backend call is now required to update the JWT
     * @param tenantId
     * @return AuthenticationResponse response
     * @throws ReturnCodeException
     */
    AuthenticationResponse switchTenant(@Nonnull String tenantId) throws ReturnCodeException;

    /** Once a language is changed, an additional backend call is now required to update the JWT.
     * This call can also be used to refresh the JWT.
     * @param tenantId
     * @return AuthenticationResponse response
     * @throws ReturnCodeException
     */
    AuthenticationResponse switchLanguage(@Nonnull String language) throws ReturnCodeException;

    /**
     * Retrieve the password change requirements from backend
     */
    GetPasswordChangeRequirementsResponse getPasswordChangeRequirements() throws ReturnCodeException;

    /** Setting of user password for testing
     * @param user
     * @param password
     * @return
     * @throws ReturnCodeException
     */
    CrudSurrogateKeyResponse setPassword(@Nonnull UserDTO user, @Nonnull String password) throws ReturnCodeException;
}
