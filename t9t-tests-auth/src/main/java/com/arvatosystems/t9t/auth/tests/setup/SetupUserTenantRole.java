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
package com.arvatosystems.t9t.auth.tests.setup;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.api.OperationType;
import com.arvatosystems.t9t.api.auth.Permissionset;
import com.arvatosystems.t9t.api.auth.UserLogLevelType;
import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.ApiKeyKey;
import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.RoleDTO;
import com.arvatosystems.t9t.auth.RoleKey;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.RoleToPermissionDTO;
import com.arvatosystems.t9t.auth.RoleToPermissionKey;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.auth.UserTenantRoleDTO;
import com.arvatosystems.t9t.auth.UserTenantRoleKey;
import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest;
import com.arvatosystems.t9t.auth.request.RoleCrudRequest;
import com.arvatosystems.t9t.auth.request.RoleToPermissionCrudRequest;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.auth.request.UserCrudRequest;
import com.arvatosystems.t9t.auth.request.UserTenantRoleCrudRequest;
import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;

// utility class to create a new tenant for a test suite. A user, a tenant and a role is created, all with the same ID
// extend this class and override methods to change the behaviour
public class SetupUserTenantRole {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetupUserTenantRole.class);
    public static final Permissionset ALL_PERMISSIONS = new Permissionset(0xfffff);

    private final ITestConnection dlg;
    private final Permissionset minPermissions;
    private final Permissionset maxPermissions;
    private final UserLogLevelType logLevel;
    private final UserLogLevelType logLevelErrors;

    public SetupUserTenantRole(final ITestConnection dlg) {
        this.dlg       = dlg;
        minPermissions = ALL_PERMISSIONS;
        maxPermissions = ALL_PERMISSIONS;
        logLevel       = UserLogLevelType.REQUESTS;
        logLevelErrors = UserLogLevelType.REQUESTS;
    }

    public SetupUserTenantRole(final ITestConnection dlg, final Permissionset minPermissions, final Permissionset maxPermissions, final UserLogLevelType logLevel, final UserLogLevelType logLevelErrors) {
        this.dlg = dlg;
        this.minPermissions = minPermissions;
        this.maxPermissions = maxPermissions;
        this.logLevel = logLevel;
        this.logLevelErrors = logLevelErrors;
    }

    /** Creates a SetupUserTenantRole with all permissions except DELETE. */
    public static SetupUserTenantRole createWithoutDelete(final ITestConnection dlg) {
        final Permissionset allWithoutDelete = new Permissionset(0xfffff);
        allWithoutDelete.remove(OperationType.DELETE);
        allWithoutDelete.freeze();
        return new SetupUserTenantRole(dlg, allWithoutDelete, allWithoutDelete, UserLogLevelType.REQUESTS, UserLogLevelType.REQUESTS);
    }

    /** Creates a SetupUserTenantRole with reduced log level, for benchmark runs. */
    public static SetupUserTenantRole createForBenchmarks(final ITestConnection dlg) {
        return new SetupUserTenantRole(dlg, ALL_PERMISSIONS, ALL_PERMISSIONS, UserLogLevelType.SESSION_ONLY, UserLogLevelType.REQUESTS);
    }

    public PermissionsDTO getPermissionDTO() {
        final PermissionsDTO permissions = new PermissionsDTO();
        permissions.setLogLevel(logLevel);
        permissions.setLogLevelErrors(logLevelErrors);
        permissions.setMinPermissions(minPermissions);
        permissions.setMaxPermissions(maxPermissions);
        permissions.setResourceIsWildcard(Boolean.TRUE);
        permissions.setResourceRestriction("");
        return permissions;
    }

    /** Creates a random password for tests (not a good one). */
    public static String createRandomSimplePWForTests() {
        final String s = UUID.randomUUID().toString();
        return s.substring(0, 16);
    }

    // this method is performed while operating under the "@" tenant
    public String createTenant(final String id) {
        final TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setTenantId(id);
        tenantDTO.setIsActive(true);
        tenantDTO.setName("localtests tenant");

        final TenantCrudRequest request = new TenantCrudRequest();
        request.setCrud(OperationType.MERGE);
        request.setData(tenantDTO);
        request.setKey(id);
        request.validate();
        return dlg.typeIO(request, CrudStringKeyResponse.class).getKey();
    }

    /** Creates a role with the given ID and a default name, in the current tenant. */
    public Long createRole(final String id) {
        return createRole(id, "localtests role");
    }

    /** Creates a role with the given ID and a specified name, in the current tenant. */
    public Long createRole(final String id, final String roleName) {
        // ensure permission to all tenants
        final RoleDTO roleDTO = new RoleDTO();
        roleDTO.setRoleId(id);
        roleDTO.setIsActive(true);
        roleDTO.setName(roleName);

        final RoleCrudRequest request = new RoleCrudRequest();
        request.setCrud(OperationType.MERGE);
        request.setData(roleDTO);
        request.setNaturalKey(new RoleKey(id));
        request.validate();
        return dlg.typeIO(request, CrudSurrogateKeyResponse.class).getKey();
    }

    private static String stripRequest(final String request) {
        if (request.endsWith("Request")) {
            return request.substring(0, request.length() - "Request".length());
        } else {
            throw new IllegalArgumentException("Request PQON does not end with 'Request': " + request);
        }
    }

    /** Add permissions to a role for the given comma separated list of IDs. */
    public void addPermissionsToRole(final String roleId, final PermissionType type, final Permissionset permissions, final String ids) {
        final RoleKey roleKey = new RoleKey(roleId);
        final String[] idArray = ids.split(",");
        for (final String id : idArray) {
            final String permissionId = type.getToken() + "." + id.trim();
            addPermissionsToRole(roleKey, permissions, permissionId);
        }
    }

    /** Add backend permissions to a role for the given list of requests. The permission ID is derived from the request PQON. */
    public void addBackendPermissionsToRole(final String roleId, final Permissionset permissions, final List<Class<? extends RequestParameters>> requestClasses) {
        final RoleKey roleKey = new RoleKey(roleId);
        for (final Class<? extends RequestParameters> rpq : requestClasses) {
            try {
                final String pqon = rpq.getDeclaredConstructor().newInstance().ret$PQON();
                final String permissionId = PermissionType.BACKEND.getToken() + "." + stripRequest(pqon);
                addPermissionsToRole(roleKey, permissions, permissionId);
            } catch (final Exception e) {
                throw new RuntimeException("Error while adding backend permissions to role " + roleId + " for class " + rpq.getCanonicalName(), e);
            }
        }
    }

    public void addPermissionsToRole(final RoleRef roleRef, final Permissionset permissions, final String permissionId) {
        final RoleToPermissionDTO permissionsDTO = new RoleToPermissionDTO();

        permissionsDTO.setRoleRef(roleRef);
        permissionsDTO.setPermissionId(permissionId);
        permissionsDTO.setPermissionSet(permissions);

        final RoleToPermissionCrudRequest request = new RoleToPermissionCrudRequest();
        request.setCrud(OperationType.MERGE);
        request.setData(permissionsDTO);
        request.setKey(new RoleToPermissionKey(roleRef, permissionId));
        request.validate();
        dlg.okIO(request);
    }

    /** Creates a user with the given ID and a default name, in the current tenant. */
    public Long createUser(final String id) {
        final UserDTO userDTO = new UserDTO();
        userDTO.setUserId(id);
        userDTO.setIsActive(true);
        userDTO.setName("localtests user");
        userDTO.setEmailAddress("dummy@void.com");
        userDTO.setPermissions(getPermissionDTO());

        final UserCrudRequest request = new UserCrudRequest();
        request.setCrud(OperationType.MERGE);
        request.setData(userDTO);
        request.setNaturalKey(new UserKey(id));
        request.validate();
        return dlg.typeIO(request, CrudSurrogateKeyResponse.class).getKey();
    }

    public Long createApiKey(final String userId, final UUID key) {
        final ApiKeyDTO apiKeyDTO = new ApiKeyDTO();
        apiKeyDTO.setUserRef(new UserKey(userId));
        apiKeyDTO.setIsActive(true);
        apiKeyDTO.setApiKey(key);
        apiKeyDTO.setName("localtest key for " + userId);
        apiKeyDTO.setPermissions(getPermissionDTO());

        final ApiKeyCrudRequest request = new ApiKeyCrudRequest();
        request.setCrud(OperationType.MERGE);
        request.setData(apiKeyDTO);
        request.setNaturalKey(new ApiKeyKey(key));
        request.validate();
        return dlg.typeIO(request, CrudSurrogateKeyResponse.class).getKey();
    }

    public void createUserTenantRole(final Long userRef, final Long roleRef) {
        final UserTenantRoleDTO utrDTO = new UserTenantRoleDTO();
        utrDTO.setUserRef(new UserRef(userRef));
        utrDTO.setRoleRef(new RoleRef(roleRef));
        final UserTenantRoleKey utrKey = new UserTenantRoleKey();
        utrKey.setUserRef(new UserRef(userRef));
        utrKey.setRoleRef(new RoleRef(roleRef));

        final UserTenantRoleCrudRequest request = new UserTenantRoleCrudRequest();
        request.setCrud(OperationType.MERGE);
        request.setData(utrDTO);
        request.setKey(utrKey);
        request.validate();
        dlg.okIO(request);
    }

    public void createUserTenantRole(final String id, final UUID apiKey, final boolean switchTo) {
        // create the tenant using the global admin
        createTenant(id);
        dlg.switchTenant(id, 0);
        // create the new user and role
        final Long userRef = createUser(id);
        final Long roleRef = createRole(id);
        final Long apiKeyRef = createApiKey(id, apiKey);

        LOGGER.info("Create user / tenant / role of ID, got refs user {} / role {}, API-Key ref is {}", userRef, roleRef, apiKeyRef);
        createUserTenantRole(userRef, roleRef);

        if (switchTo) {
            dlg.switchUser(apiKey);
        }
    }

    public void createUserWithTenantRole(final String id, final UUID apiKey) {
        // create the new user and role
        final Long userRef = createUser(id);
        final Long roleRef = createRole(id);
        final Long apiKeyRef = createApiKey(id, apiKey);

        LOGGER.info("Create user / tenant / role of ID, got refs user {} / role {}, API-Key ref is {}", userRef, roleRef, apiKeyRef);
        createUserTenantRole(userRef, roleRef);
    }
}
