/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.be.impl;

import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.base.T9tConstants;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;

/**
 * Util class to provide default UserDTO and TenantDTO in case no other
 * suitable instances are available.
 * All instances are frozen (immutable).
 */
public final class T9tDefaultContext {

    private T9tDefaultContext() { }

    public static final JwtInfo DEFAULT_JWT = new JwtInfo();
    static {
        DEFAULT_JWT.setUserId(T9tConstants.TECHNICAL_USER_ID);
        DEFAULT_JWT.setUserRef(T9tConstants.TECHNICAL_USER_REF);
        DEFAULT_JWT.setLogLevel(UserLogLevelType.STEALTH);
        DEFAULT_JWT.setLogLevelErrors(UserLogLevelType.MESSAGE_ENTRY);
        DEFAULT_JWT.setName("t9t system user");
        DEFAULT_JWT.setLocale("en-US");
        DEFAULT_JWT.freeze();
    }

    public static final TenantDTO DEFAULT_TENANT_DTO = new TenantDTO();
    static {
        DEFAULT_TENANT_DTO.setTenantId(T9tConstants.GLOBAL_TENANT_ID);
        DEFAULT_TENANT_DTO.setIsActive(true);
        DEFAULT_TENANT_DTO.setName("Global tenant");
        DEFAULT_TENANT_DTO.freeze();
    }

    public static final UserDTO DEFAULT_USER_DTO = new UserDTO();
    static {
        DEFAULT_USER_DTO.setObjectRef(T9tConstants.TECHNICAL_USER_REF);
        DEFAULT_USER_DTO.setUserId(T9tConstants.TECHNICAL_USER_ID);
        DEFAULT_USER_DTO.setIsActive(true);
        DEFAULT_USER_DTO.setName("t9t system user");
        DEFAULT_USER_DTO.freeze();
    }

    public static final UserDTO STARTUP_USER_DTO = new UserDTO();
    static {
        STARTUP_USER_DTO.setObjectRef(T9tConstants.STARTUP_USER_REF);
        STARTUP_USER_DTO.setUserId(T9tConstants.STARTUP_USER_ID);
        STARTUP_USER_DTO.setIsActive(true);
        STARTUP_USER_DTO.setName("t9t system bootstrap user");
        STARTUP_USER_DTO.freeze();
    }

    public static final JwtInfo STARTUP_JWT = new JwtInfo();
    static {
        STARTUP_JWT.setUserRef(T9tConstants.STARTUP_USER_REF);
        STARTUP_JWT.setUserId(T9tConstants.STARTUP_USER_ID);
        STARTUP_JWT.setTenantId(T9tConstants.GLOBAL_TENANT_ID);
        STARTUP_JWT.setLogLevel(UserLogLevelType.STEALTH);
        STARTUP_JWT.setLogLevelErrors(UserLogLevelType.MESSAGE_ENTRY);
        STARTUP_JWT.setResource("-");
        STARTUP_JWT.setPermissionsMin(new Permissionset(0));
        STARTUP_JWT.setPermissionsMax(new Permissionset(0));
        STARTUP_JWT.setName("t9t system bootstrap user");
        STARTUP_JWT.setLocale("en-US");
        STARTUP_JWT.setZoneinfo("UTC");
    }
}
