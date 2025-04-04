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
package com.arvatosystems.t9t.auth;

import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest;
import com.arvatosystems.t9t.auth.request.ApiKeySearchRequest;
import com.arvatosystems.t9t.auth.request.AuthModuleCfgCrudRequest;
import com.arvatosystems.t9t.auth.request.AuthModuleCfgSearchRequest;
import com.arvatosystems.t9t.auth.request.PasswordBlacklistCrudRequest;
import com.arvatosystems.t9t.auth.request.PasswordBlacklistSearchRequest;
import com.arvatosystems.t9t.auth.request.RoleCrudRequest;
import com.arvatosystems.t9t.auth.request.RoleSearchRequest;
import com.arvatosystems.t9t.auth.request.RoleToPermissionCrudRequest;
import com.arvatosystems.t9t.auth.request.RoleToPermissionSearchRequest;
import com.arvatosystems.t9t.auth.request.SessionSearchRequest;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.auth.request.TenantLogoCrudRequest;
import com.arvatosystems.t9t.auth.request.TenantLogoSearchRequest;
import com.arvatosystems.t9t.auth.request.TenantSearchRequest;
import com.arvatosystems.t9t.auth.request.UserCrudRequest;
import com.arvatosystems.t9t.auth.request.UserSearchRequest;
import com.arvatosystems.t9t.auth.request.UserTenantRoleCrudRequest;
import com.arvatosystems.t9t.auth.request.UserTenantRoleSearchRequest;
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.entities.SessionTracking;

import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

public final class T9tAuthModels implements IViewModelContainer {

    private static final CrudViewModel<TenantDTO, FullTrackingWithVersion> TENANT_VIEW_MODEL
      = new CrudViewModel<>(
        TenantDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        TenantSearchRequest.BClass.INSTANCE,
        TenantCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<UserDTO, FullTrackingWithVersion> USER_VIEW_MODEL
      = new CrudViewModel<>(
        UserDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        UserSearchRequest.BClass.INSTANCE,
        UserCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<RoleDTO, FullTrackingWithVersion> ROLE_VIEW_MODEL
      = new CrudViewModel<>(
        RoleDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        RoleSearchRequest.BClass.INSTANCE,
        RoleCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<ApiKeyDTO, FullTrackingWithVersion> APIKEY_VIEW_MODEL
      = new CrudViewModel<>(
        ApiKeyDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ApiKeySearchRequest.BClass.INSTANCE,
        ApiKeyCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<SessionDTO, SessionTracking> SESSION_VIEW_MODEL
      = new CrudViewModel<>(
        SessionDTO.BClass.INSTANCE,
        SessionTracking.BClass.INSTANCE,
        SessionSearchRequest.BClass.INSTANCE,
        null);
    private static final CrudViewModel<RoleToPermissionDTO, FullTrackingWithVersion> ROLE_PERMISSION_VIEW_MODEL
      = new CrudViewModel<>(
        RoleToPermissionDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        RoleToPermissionSearchRequest.BClass.INSTANCE,
        RoleToPermissionCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<UserTenantRoleDTO, FullTrackingWithVersion> USER_TENANT_ROLE_VIEW_MODEL
      = new CrudViewModel<>(
        UserTenantRoleDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        UserTenantRoleSearchRequest.BClass.INSTANCE,
        UserTenantRoleCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<AuthModuleCfgDTO, FullTrackingWithVersion> AUTH_MODULE_CFG_VIEW_MODEL
      = new CrudViewModel<>(
        AuthModuleCfgDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        AuthModuleCfgSearchRequest.BClass.INSTANCE,
        AuthModuleCfgCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<TenantLogoDTO, FullTrackingWithVersion> TENANT_LOGO_VIEW_MODEL
      = new CrudViewModel<>(
        TenantLogoDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        TenantLogoSearchRequest.BClass.INSTANCE,
        TenantLogoCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<ResetPasswordRequest, TrackingBase> RESET_PWD_VIEW_MODEL
      = new CrudViewModel<>(
        ResetPasswordRequest.BClass.INSTANCE,
        null,
        null,
        null);
    private static final CrudViewModel<PasswordBlacklistDTO, NoTracking> PASSWORD_BLACKLIST_VIEW_MODEL
      = new CrudViewModel<>(
        PasswordBlacklistDTO.BClass.INSTANCE,
        NoTracking.BClass.INSTANCE,
        PasswordBlacklistSearchRequest.BClass.INSTANCE,
        PasswordBlacklistCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("sessions",          SESSION_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("tenant",            TENANT_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("user",              USER_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("role",              ROLE_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("apikey",            APIKEY_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("roleToPermission",  ROLE_PERMISSION_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("userTenantRole",    USER_TENANT_ROLE_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("authModuleCfg",     AUTH_MODULE_CFG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("tenantLogo",        TENANT_LOGO_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("resetPwd",          RESET_PWD_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("passwordBlacklist", PASSWORD_BLACKLIST_VIEW_MODEL);
    }
}
