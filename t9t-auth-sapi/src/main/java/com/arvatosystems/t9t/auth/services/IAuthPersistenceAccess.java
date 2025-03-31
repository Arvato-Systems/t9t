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
package com.arvatosystems.t9t.auth.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.SessionDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.base.auth.ExternalTokenAuthenticationParam;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.OperationTypes;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface IAuthPersistenceAccess {

    //changeMe to correct specification
    AuthModuleCfgDTO DEFAULT_MODULE_CFG = new AuthModuleCfgDTO(
        null,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
        1, 2, 1, 1, 1, 0, // additional min requirements for password changes
        false, true, // settings for password checks against blacklist
        false, false, false, false // flags for user notification
        );

    // returns permission entries from the database which are relevant for the user / tenant as specified by the jwtInfo record, and which are relevant for the
    // specified resource. These are all resourceIds which are a substring of resource

    List<PermissionEntry> getAllDBPermissions(@Nonnull JwtInfo jwtInfo);

    /** Returns the Pair of <tenantId, UserDTO> of the user specified by userId, or null if there is no such user. */
    DataWithTrackingS<UserDTO, FullTrackingWithVersion> getUserById(String userId);

    /** Checks authentication by API key. */
    AuthIntermediateResult getByApiKey(@Nonnull Instant now, @Nonnull UUID key);

    /** Checks authentication by userId / password. */
    AuthIntermediateResult getByUserIdAndPassword(@Nonnull Instant now, @Nonnull String userId, @Nonnull String password, @Nullable String newPassword);

    /** Checks authentication by OpenID Connect token. */
    AuthIntermediateResult getByExternalToken(@Nonnull Instant now, @Nonnull ExternalTokenAuthenticationParam authParam);

    void storeSession(SessionDTO session);
    List<TenantDescription> getAllTenantsForUser(RequestContext ctx, Long userRef);

    Map<String, Object> getUserZ(Long userRef);
    Map<String, Object> getTenantZ(String tenantId);

    String assignNewPasswordIfEmailMatches(RequestContext ctx, String userId, String emailAddress);

    void deletePasswordBlacklist();

    List<UserData> getUsersWithPermission(JwtInfo jwtInfo, PermissionType permissionType, String resourceId, OperationTypes operationTypes);
}
