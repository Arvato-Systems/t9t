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
package com.arvatosystems.t9t.embedded.tests.simple

import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.request.RoleCrudRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRoleNoDeletePermissions
import com.arvatosystems.t9t.embedded.connect.Connection
import de.jpaw.bonaparte.pojos.api.OperationType
import java.util.UUID
import org.junit.Test
import com.arvatosystems.t9t.base.T9tException

class ITCrud {

    @Test
    def public void noDeletePermissionTest() {
        val dlg = new Connection

        val setup = new SetupUserTenantRoleNoDeletePermissions(dlg)

        val newKey = UUID.randomUUID
        setup.createUserTenantRole("crud", newKey, true)

        setup.createRole("bladi")
        dlg.errIO(new RoleCrudRequest => [
            crud            = OperationType.DELETE
            naturalKey      = new RoleKey("bladi")
        ], T9tException.NOT_AUTHORIZED)
    }
}
