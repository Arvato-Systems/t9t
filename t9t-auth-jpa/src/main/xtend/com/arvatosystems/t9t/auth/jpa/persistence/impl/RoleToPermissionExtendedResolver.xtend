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
package com.arvatosystems.t9t.auth.jpa.persistence.impl

import com.arvatosystems.t9t.auth.RoleToPermissionInternalKey
import com.arvatosystems.t9t.auth.RoleToPermissionKey
import com.arvatosystems.t9t.auth.RoleToPermissionRef
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.dp.Specializes

@Specializes
@Singleton
class RoleToPermissionExtendedResolver extends RoleToPermissionEntityResolver {

    @Inject IRoleEntityResolver roleResolver;

    override protected RoleToPermissionRef resolveNestedRefs(RoleToPermissionRef ref) {
        if (ref instanceof RoleToPermissionKey) {
            return new RoleToPermissionInternalKey => [
                roleRef      = roleResolver.getRef(ref.roleRef, false)
                permissionId = ref.permissionId
                tenantRef    = sharedTenantRef
            ]
        }
        return super.resolveNestedRefs(ref);
    }
}
