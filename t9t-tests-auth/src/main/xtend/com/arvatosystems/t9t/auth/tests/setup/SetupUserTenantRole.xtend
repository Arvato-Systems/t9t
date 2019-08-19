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
package com.arvatosystems.t9t.auth.tests.setup

import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.ApiKeyKey
import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.RoleDTO
import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.RoleRef
import com.arvatosystems.t9t.auth.TenantDTO
import com.arvatosystems.t9t.auth.TenantKey
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.UserRef
import com.arvatosystems.t9t.auth.UserTenantRoleDTO
import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest
import com.arvatosystems.t9t.auth.request.RoleCrudRequest
import com.arvatosystems.t9t.auth.request.TenantCrudRequest
import com.arvatosystems.t9t.auth.request.UserCrudRequest
import com.arvatosystems.t9t.auth.request.UserTenantRoleCrudRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import java.util.UUID
import com.arvatosystems.t9t.auth.UserTenantRoleKey

// utility class to create a new tenant for a test suite. A user, a tenant and a role is created, all with the same ID
// extend this class and override methods to change the behaviour
@AddLogger
class SetupUserTenantRole {
    public static val ALL_PERMISSIONS = new Permissionset(0xfffff)  // .fromStringMap("XSLCRUDIAVMP")
    private final ITestConnection dlg

    new(ITestConnection dlg) {
        this.dlg = dlg
    }

    def getPermissionset(boolean isMax) {
        return ALL_PERMISSIONS
    }

    def getPermissionDTO() {
        return new PermissionsDTO => [
            logLevel            = UserLogLevelType.REQUESTS
            logLevelErrors      = UserLogLevelType.REQUESTS
            minPermissions      = getPermissionset(false)
            maxPermissions      = getPermissionset(true)
            resourceIsWildcard  = Boolean.TRUE
            resourceRestriction = ""
        ]
    }

    // this method is performed while operating under the "@" tenant
    def Long createTenant(String id) {
        val tenantDTO       = new TenantDTO => [
            tenantId        = id
            isActive        = true
            name            = "localtests tenant"
        ]
        return dlg.typeIO(new TenantCrudRequest => [
            crud            = OperationType.MERGE
            data            = tenantDTO
            naturalKey      = new TenantKey(id)
            validate
        ], CrudSurrogateKeyResponse).key
    }

    def Long createRole(String id) {
        // ensure permission to all tenants
        val roleDTO         = new RoleDTO => [
            roleId          = id
            isActive        = true
            name            = "localtests role"
        ]
        return dlg.typeIO(new RoleCrudRequest => [
            crud            = OperationType.MERGE
            data            = roleDTO
            naturalKey      = new RoleKey(id)
            validate
        ], CrudSurrogateKeyResponse).key
    }

    def Long createUser(String id) {
        val userDTO = new UserDTO => [
            userId          = id
            isActive        = true
            name            = "localtests user"
            emailAddress    = "dummy@void.com"
            permissions     = permissionDTO
        ]
        return dlg.typeIO(new UserCrudRequest => [
            crud            = OperationType.MERGE
            data            = userDTO
            naturalKey      = new UserKey(id)
            validate
        ], CrudSurrogateKeyResponse).key
    }

    def Long createApiKey(String userId, UUID key) {
        val apiKeyDTO       = new ApiKeyDTO => [
            userRef         = new UserKey(userId)
            isActive        = true
            apiKey          = key
            name            = 'localtest key for ' + userId
            permissions     = getPermissionDTO
        ]
        return dlg.typeIO(new ApiKeyCrudRequest => [
            crud            = OperationType.MERGE
            data            = apiKeyDTO
            naturalKey      = new ApiKeyKey(key)
            validate
        ], CrudSurrogateKeyResponse).key
    }

    def void createUserTenantRole(Long userRef, Long roleRef) {
        val utrDTO = new UserTenantRoleDTO
        utrDTO.userRef = new UserRef(userRef)
        utrDTO.roleRef = new RoleRef(roleRef)
        val utrKey = new UserTenantRoleKey
        utrKey.userRef = new UserRef(userRef)
        utrKey.roleRef = new RoleRef(roleRef)
        dlg.okIO(new UserTenantRoleCrudRequest => [
            crud            = OperationType.MERGE
            data            = utrDTO
            key             = utrKey
            validate
        ])
    }

    def void createUserTenantRole(String id, UUID apiKey, boolean switchTo) {
        // create the tenant using the global admin
        val tenantRef = createTenant(id)
        dlg.switchTenant(id, 0)
        // create the new user and role
        val userRef = createUser(id)
        val roleRef = createRole(id)
        val apiKeyRef = createApiKey(id, apiKey)

        LOGGER.info("Create user / tenant / role of ID, got refs {} / {} / {}, API-Key ref is {}", userRef, tenantRef, roleRef, apiKeyRef)
        createUserTenantRole(userRef, roleRef)

        if (switchTo) {
            dlg.switchUser(apiKey)
        }
    }
}
