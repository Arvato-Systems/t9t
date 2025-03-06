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
package com.arvatosystems.t9t.embedded.tests.user

import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.RoleToPermissionDTO
import com.arvatosystems.t9t.auth.request.RoleToPermissionCrudRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.authz.api.QueryUsersWithPermissionRequest
import com.arvatosystems.t9t.authz.api.QueryUsersWithPermissionResponse
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.auth.PermissionType
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.OperationTypes
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import java.util.UUID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled

/*H2 database doesn't support bitwise operator '&' */
@Disabled
class UsersWithPermissionTest {
    static ITestConnection dlg
    static final String myPermissionId = "U.testperm-id.x"

    static private class MySetup extends SetupUserTenantRole {
        new(ITestConnection dlg) {
            super(dlg)
        }

        override getPermissionDTO() {
            return new PermissionsDTO => [
                logLevel = UserLogLevelType.REQUESTS
                logLevelErrors = UserLogLevelType.REQUESTS
                minPermissions = Permissionset.ofTokens(OperationType.LOOKUP)
                maxPermissions = ALL_PERMISSIONS
                resourceIsWildcard = Boolean.TRUE
                resourceRestriction = "B.,"
            ]
        }
    }

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection;
        (new MySetup(dlg)).createUserTenantRole("testPerm", UUID.randomUUID, true)

        val rtp = new RoleToPermissionDTO => [
            roleRef = new RoleKey("testPerm")
            permissionId = myPermissionId
            permissionSet = Permissionset.ofTokens(OperationType.APPROVE)
            validate
        ]
        dlg.okIO(new RoleToPermissionCrudRequest => [
            crud = OperationType.CREATE
            data = rtp
        ])

        (new MySetup(dlg)).createUserWithTenantRole("testPerm_2", UUID.randomUUID)

        val rtp2 = new RoleToPermissionDTO => [
            roleRef = new RoleKey("testPerm_2")
            permissionId = myPermissionId
            permissionSet = Permissionset.ofTokens(OperationType.EXPORT)
            validate
        ]
        dlg.okIO(new RoleToPermissionCrudRequest => [
            crud = OperationType.CREATE
            data = rtp2
        ])

    }

    @Test
    def void QueryUsersWithPermissionRequest() {
        val result = dlg.typeIO(new QueryUsersWithPermissionRequest(PermissionType.FRONTEND, "testperm-id.x.abc", OperationTypes.ofTokens(OperationType.APPROVE)),
            QueryUsersWithPermissionResponse)
        println('''Result is «result.toString»''')
        Assertions.assertEquals(1, result.users.size)
    }
}
