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
package com.arvatosystems.t9t.auth.mocks

import com.arvatosystems.t9t.server.services.IAuthorize
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.dp.Fallback
import de.jpaw.dp.Singleton
import com.arvatosystems.t9t.base.auth.PermissionEntry
import de.jpaw.bonaparte.pojos.api.OperationType
import com.arvatosystems.t9t.base.auth.PermissionType

@Fallback
@Singleton
@AddLogger
class AuthorizationMock implements IAuthorize {
    private static final Permissionset NO_PERMISSIONS = new Permissionset

    override getPermissions(JwtInfo jwtInfo, PermissionType permissionType, String resource) {
        LOGGER.debug("Permission requested for user {}, tenant {}, type {}, resource {}", jwtInfo.userId, jwtInfo.tenantId, permissionType, resource);

        return jwtInfo.permissionsMin ?: NO_PERMISSIONS
    }

    override getAllPermissions(JwtInfo jwtInfo, PermissionType permissionType) {
        LOGGER.debug("Full permission list requested for user {}, tenant {}, type {}", jwtInfo.userId, jwtInfo.tenantId, permissionType);
        if (permissionType == PermissionType.BACKEND)
            return #[ new PermissionEntry("B.testRequest", Permissionset.ofTokens(OperationType.EXECUTE))]
        else
            return #[]
    }
}
