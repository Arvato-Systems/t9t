/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.be.auth.nodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.request.PingRequest;
import com.arvatosystems.t9t.base.request.ProcessStatusRequest;
import com.arvatosystems.t9t.base.request.RetrieveComponentInfoRequest;
import com.arvatosystems.t9t.base.request.TerminateProcessRequest;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.dp.Singleton;

@Singleton
public class AuthorizeNoDbBackend implements IAuthorize {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizeNoDbBackend.class);
    private static final Permissionset NO_PERMISSIONS = new Permissionset();
    private static final List<PermissionEntry> BACKEND_PERMISSIONS = new ArrayList<>(10);

    private void addPermission(final ClassDefinition cd) {
        final PermissionEntry newEntry = new PermissionEntry("B." +  cd.getName(), Permissionset.ofTokens(OperationType.EXECUTE));
        newEntry.freeze();
        BACKEND_PERMISSIONS.add(newEntry);
    }

    public AuthorizeNoDbBackend() {
        addPermission(PingRequest.class$MetaData());
        addPermission(RetrieveComponentInfoRequest.class$MetaData());
        addPermission(ProcessStatusRequest.class$MetaData());
        addPermission(TerminateProcessRequest.class$MetaData());
        final List<String> permittedRequests = ConfigProvider.getConfiguration().getNoDbBackendPermittedRequests();
        if (permittedRequests != null) {
            for (final String pqonToPermit: permittedRequests) {
                final PermissionEntry newEntry = new PermissionEntry("B." +  pqonToPermit, Permissionset.ofTokens(OperationType.EXECUTE));
                newEntry.freeze();
                BACKEND_PERMISSIONS.add(newEntry);
            }
        }
    }

    @Override
    public List<PermissionEntry> getAllPermissions(final JwtInfo jwtInfo, final PermissionType permissionType) {
        LOGGER.debug("Full permission list requested for user {}, tenant {}, type {}", jwtInfo.getUserId(), jwtInfo.getTenantId(), permissionType);
        if (permissionType == PermissionType.BACKEND)
            return Collections.unmodifiableList(BACKEND_PERMISSIONS);
        else
            return Collections.emptyList();
    }

    @Override
    public Permissionset getPermissions(final JwtInfo jwtInfo, final PermissionType permissionType, final String resource) {
        LOGGER.debug("Permission requested for user {}, tenant {}, type {}, resource {}", jwtInfo.getUserId(), jwtInfo.getTenantId(), permissionType, resource);

        return jwtInfo.getPermissionsMin() == null ? NO_PERMISSIONS : jwtInfo.getPermissionsMin();
    }
}
