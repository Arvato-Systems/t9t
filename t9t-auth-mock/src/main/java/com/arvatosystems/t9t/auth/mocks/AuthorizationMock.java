/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.mocks;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Singleton
public class AuthorizationMock implements IAuthorize {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationMock.class);
    private static final Permissionset NO_PERMISSIONS = new Permissionset();

    @Override
    public Permissionset getPermissions(final JwtInfo jwtInfo, final PermissionType permissionType, final String resource) {
        LOGGER.debug("Permission requested for user {}, tenant {}, type {}, resource {}", jwtInfo.getUserId(), jwtInfo.getTenantId(), permissionType, resource);
        if (jwtInfo.getPermissionsMin() != null) {
            return jwtInfo.getPermissionsMin();
        }
        return NO_PERMISSIONS;
    }

    @Override
    public List<PermissionEntry> getAllPermissions(final JwtInfo jwtInfo, final PermissionType permissionType) {
        LOGGER.debug("Full permission list requested for user {}, tenant {}, type {}", jwtInfo.getUserId(), jwtInfo.getTenantId(), permissionType);
        if (PermissionType.BACKEND == permissionType) {
            final PermissionEntry backendPermissionEntry = new PermissionEntry("B.testRequest", Permissionset.ofTokens(OperationType.EXECUTE));
            return Collections.singletonList(backendPermissionEntry);
        } else {
            return Collections.emptyList();
        }
    }
}
