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
package com.arvatosystems.t9t.server.services;

import java.util.List;

import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public interface IAuthorize {
    public static final Permissionset NO_PERMISSIONS = new Permissionset();
    public static final Permissionset EXEC_PERMISSION = new Permissionset(1 << OperationType.EXECUTE.ordinal());
    public static final Permissionset ALL_PERMISSIONS = new Permissionset(0xfffff);  // 20 permission bits all set

    Permissionset getPermissions(JwtInfo jwtInfo, PermissionType permissionType, String resource);
    List<PermissionEntry> getAllPermissions(JwtInfo jwtInfo, PermissionType permissionType);
}
