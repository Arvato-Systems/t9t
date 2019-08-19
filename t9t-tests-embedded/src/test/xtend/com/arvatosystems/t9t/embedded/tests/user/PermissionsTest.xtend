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
package com.arvatosystems.t9t.embedded.tests.user

import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.RoleToPermissionDTO
import com.arvatosystems.t9t.auth.request.RoleToPermissionCrudRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.authz.api.QueryPermissionsRequest
import com.arvatosystems.t9t.authz.api.QueryPermissionsResponse
import com.arvatosystems.t9t.authz.api.QuerySinglePermissionRequest
import com.arvatosystems.t9t.authz.api.QuerySinglePermissionResponse
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.auth.PermissionEntry
import com.arvatosystems.t9t.base.auth.PermissionType
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import de.jpaw.bonaparte.util.ToStringHelper
import java.util.UUID
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class PermissionsTest {
    static private ITestConnection dlg
    static private final String myPermissionId = "U.testperm-id.x"

    static private class MySetup extends SetupUserTenantRole {
        new(ITestConnection dlg) {
            super(dlg)
        }
        override getPermissionDTO() {
            return new PermissionsDTO => [
                logLevel            = UserLogLevelType.REQUESTS
                logLevelErrors      = UserLogLevelType.REQUESTS
                minPermissions      = Permissionset.ofTokens(OperationType.LOOKUP)
                maxPermissions      = ALL_PERMISSIONS
                resourceIsWildcard  = Boolean.TRUE
                resourceRestriction = "B.,"
            ]
        }
    }

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection;
        (new MySetup(dlg)).createUserTenantRole("testPerm", UUID.randomUUID, true)

        val rtp = new RoleToPermissionDTO => [
            roleRef          = new RoleKey("testPerm")
            permissionId     = myPermissionId
            permissionSet    = Permissionset.ofTokens(OperationType.CONTEXT)
            validate
        ]
        dlg.okIO(new RoleToPermissionCrudRequest => [
            crud = OperationType.CREATE
            data = rtp
        ])
    }

    @Test
    def public void QueryPermissionsTest() {
        val result = dlg.typeIO(new QueryPermissionsRequest(PermissionType.FRONTEND), QueryPermissionsResponse)
        println('''Result is «ToStringHelper.toStringML(result)»''')
        Assert.assertEquals(result.permissions.size, 1)
        Assert.assertEquals(new PermissionEntry(myPermissionId, Permissionset.ofTokens(OperationType.CONTEXT, OperationType.LOOKUP)), result.permissions.get(0))
    }

    @Test
    def public void QuerySinglePermissionTest() {
        val result = dlg.typeIO(new QuerySinglePermissionRequest(PermissionType.FRONTEND, "testperm-id.x"), QuerySinglePermissionResponse)
        println('''Result is «ToStringHelper.toStringML(result)»''')
        Assert.assertEquals(Permissionset.ofTokens(OperationType.CONTEXT, OperationType.LOOKUP), result.permissions)
    }

    @Test
    def public void QueryAnotherSinglePermissionTest() {
        val result = dlg.typeIO(new QuerySinglePermissionRequest(PermissionType.FRONTEND, "no-perm"), QuerySinglePermissionResponse)
        println('''Result is «ToStringHelper.toStringML(result)»''')
        Assert.assertEquals(Permissionset.ofTokens(), result.permissions)
    }
}
