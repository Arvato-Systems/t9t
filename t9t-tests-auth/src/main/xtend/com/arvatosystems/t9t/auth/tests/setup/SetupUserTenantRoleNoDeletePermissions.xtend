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
package com.arvatosystems.t9t.auth.tests.setup

import com.arvatosystems.t9t.base.ITestConnection
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.Permissionset

// sets up a user who cannot delete anything
class SetupUserTenantRoleNoDeletePermissions extends SetupUserTenantRole {
    private static val ALL_PERMISSIONS_EXCEPT_DELETE = {
        val all = new Permissionset(0xfffff)
        all.remove(OperationType.DELETE)
        all.freeze
        all
    }

    new(ITestConnection dlg) {
        super(dlg)
    }

    override getPermissionset(boolean isMax) {
        return ALL_PERMISSIONS_EXCEPT_DELETE
    }
}
