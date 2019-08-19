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
package com.arvatosystems.t9t.auth.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.annotations.jpa.GlobalTenantCanAccessAll
import com.arvatosystems.t9t.auth.ApiKeyRef
import com.arvatosystems.t9t.auth.PasswordKey
import com.arvatosystems.t9t.auth.RoleRef
import com.arvatosystems.t9t.auth.RoleToPermissionRef
import com.arvatosystems.t9t.auth.SessionRef
import com.arvatosystems.t9t.auth.TenantRef
import com.arvatosystems.t9t.auth.UserRef
import com.arvatosystems.t9t.auth.UserTenantRoleRef
import com.arvatosystems.t9t.auth.jpa.entities.ApiKeyEntity
import com.arvatosystems.t9t.auth.jpa.entities.AuthModuleCfgEntity
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity
import com.arvatosystems.t9t.auth.jpa.entities.RoleEntity
import com.arvatosystems.t9t.auth.jpa.entities.RoleToPermissionEntity
import com.arvatosystems.t9t.auth.jpa.entities.SessionEntity
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity
import com.arvatosystems.t9t.auth.jpa.entities.TenantLogoEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserTenantRoleEntity
import java.util.List

@AutoResolver42
class AuthResolvers {
    @AllCanAccessGlobalTenant
    def AuthModuleCfgEntity     getAuthModuleCfgEntity(Long ref, boolean onlyActive) { return null; }

    @AllCanAccessGlobalTenant
    def TenantLogoEntity        getTenantLogoEntity   (Long ref, boolean onlyActive) { return null; }

    @GlobalTenantCanAccessAll   // admin access to all tenant's data
    def SessionEntity           getSessionEntity(SessionRef entityRef, boolean onlyActive) { return null; }

    @GlobalTenantCanAccessAll   // for Users, the admin can manage users of all tenants
    @AllCanAccessGlobalTenant   // if I'm a global user but logged on using a specific tenant, I must see my data as well
    def UserEntity              getUserEntity(UserRef ref, boolean onlyActive) { return null; }
    def UserEntity              findByUserId(boolean onlyActive, String userId) { return null; }


    @GlobalTenantCanAccessAll   // for Tenants, the admin can manage all
    @AllCanAccessGlobalTenant
    def TenantEntity            getTenantEntity(TenantRef ref, boolean onlyActive) { return null; }

    @GlobalTenantCanAccessAll
    @AllCanAccessGlobalTenant
    def RoleEntity              getRoleEntity(RoleRef entityRef, boolean onlyActive) { return null; }
    def List<RoleEntity>        findByRoleIdWithDefault (boolean onlyActive, String roleId) { return null; }

    // UserTenantRoles is category A + D (global access for @, default access for other users)
    @AllCanAccessGlobalTenant
    @GlobalTenantCanAccessAll
    def UserTenantRoleEntity    getUserTenantRoleEntity(UserTenantRoleRef entityRef, boolean onlyActive) { return null; }

    // RoleToPermissions is category A + D (global access for @, default access for other users)
    @AllCanAccessGlobalTenant
    @GlobalTenantCanAccessAll
    def RoleToPermissionEntity  getRoleToPermissionEntity(RoleToPermissionRef entityRef, boolean onlyActive) { return null; }


    def ApiKeyEntity            getApiKeyEntity (ApiKeyRef ref,    boolean onlyActive) { return null; }
    def PasswordEntity          getPasswordEntity(PasswordKey key, boolean onlyActive) { return null; }
}
