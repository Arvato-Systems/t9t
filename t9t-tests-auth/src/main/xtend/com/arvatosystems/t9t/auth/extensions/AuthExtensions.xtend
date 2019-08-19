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
package com.arvatosystems.t9t.auth.extensions

import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.ApiKeyKey
import com.arvatosystems.t9t.auth.AuthModuleCfgDTO
import com.arvatosystems.t9t.auth.RoleDTO
import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.RoleRef
import com.arvatosystems.t9t.auth.RoleToPermissionDTO
import com.arvatosystems.t9t.auth.RoleToPermissionKey
import com.arvatosystems.t9t.auth.RoleToPermissionRef
import com.arvatosystems.t9t.auth.TenantDTO
import com.arvatosystems.t9t.auth.TenantKey
import com.arvatosystems.t9t.auth.TenantLogoDTO
import com.arvatosystems.t9t.auth.TenantRef
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.UserRef
import com.arvatosystems.t9t.auth.UserTenantRoleDTO
import com.arvatosystems.t9t.auth.UserTenantRoleKey
import com.arvatosystems.t9t.auth.UserTenantRoleRef
import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest
import com.arvatosystems.t9t.auth.request.AuthModuleCfgCrudRequest
import com.arvatosystems.t9t.auth.request.RoleCrudRequest
import com.arvatosystems.t9t.auth.request.RoleToPermissionCrudRequest
import com.arvatosystems.t9t.auth.request.TenantCrudRequest
import com.arvatosystems.t9t.auth.request.TenantLogoCrudRequest
import com.arvatosystems.t9t.auth.request.UserCrudRequest
import com.arvatosystems.t9t.auth.request.UserTenantRoleCrudRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyResponse
import com.arvatosystems.t9t.base.crud.CrudModuleCfgResponse
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import de.jpaw.bonaparte.pojos.api.OperationType

class AuthExtensions {
    // extension methods for the types with surrogate keys
    def static CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion> merge(UserDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new UserCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new UserKey(dto.userId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<TenantDTO, FullTrackingWithVersion> merge(TenantDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new TenantCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new TenantKey(dto.tenantId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<RoleDTO, FullTrackingWithVersion> merge(RoleDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new RoleCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new RoleKey(dto.roleId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ApiKeyDTO, FullTrackingWithVersion> merge(ApiKeyDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ApiKeyCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new ApiKeyKey(dto.apiKey)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudCompositeKeyResponse<UserTenantRoleRef, UserTenantRoleDTO, FullTrackingWithVersion> create(UserTenantRoleDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new UserTenantRoleCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudCompositeKeyResponse)
    }
    def static CrudCompositeKeyResponse<RoleToPermissionRef, RoleToPermissionDTO, FullTrackingWithVersion> create(RoleToPermissionDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new RoleToPermissionCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudCompositeKeyResponse)
    }
    def static CrudCompositeKeyResponse<UserTenantRoleRef, UserTenantRoleDTO, FullTrackingWithVersion> merge(UserTenantRoleDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new UserTenantRoleCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            key             = new UserTenantRoleKey(dto.userRef, dto.roleRef)
        ], CrudCompositeKeyResponse)
    }
    def static CrudCompositeKeyResponse<RoleToPermissionRef, RoleToPermissionDTO, FullTrackingWithVersion> merge(RoleToPermissionDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new RoleToPermissionCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            key             = new RoleToPermissionKey(dto.roleRef, dto.permissionId)
        ], CrudCompositeKeyResponse)
    }

    def static merge(AuthModuleCfgDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new AuthModuleCfgCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
        ], CrudModuleCfgResponse)
    }

    def static merge(TenantLogoDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new TenantLogoCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
        ], CrudModuleCfgResponse)
    }


    def static UserDTO read(UserRef ref, ITestConnection dlg) {
        return ((dlg.typeIO(new UserCrudRequest => [
            crud            = OperationType.READ
            key             = ref.objectRef
            naturalKey      = if (ref instanceof UserKey) ref
        ], CrudSurrogateKeyResponse)) as CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion>).data
    }
    def static RoleDTO read(RoleRef ref, ITestConnection dlg) {
        return ((dlg.typeIO(new RoleCrudRequest => [
            crud            = OperationType.READ
            key             = ref.objectRef
            naturalKey      = if (ref instanceof RoleKey) ref
        ], CrudSurrogateKeyResponse)) as CrudSurrogateKeyResponse<RoleDTO, FullTrackingWithVersion>).data
    }
    def static TenantDTO read(TenantRef ref, ITestConnection dlg) {
        return ((dlg.typeIO(new TenantCrudRequest => [
            crud            = OperationType.READ
            key             = ref.objectRef
            naturalKey      = if (ref instanceof TenantKey) ref
        ], CrudSurrogateKeyResponse)) as CrudSurrogateKeyResponse<TenantDTO, FullTrackingWithVersion>).data
    }
}
