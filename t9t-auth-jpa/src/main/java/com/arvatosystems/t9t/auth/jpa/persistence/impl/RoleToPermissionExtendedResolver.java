/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.jpa.persistence.impl;

import com.arvatosystems.t9t.auth.RoleToPermissionInternalKey;
import com.arvatosystems.t9t.auth.RoleToPermissionKey;
import com.arvatosystems.t9t.auth.RoleToPermissionRef;
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Specializes
@Singleton
public class RoleToPermissionExtendedResolver extends RoleToPermissionEntityResolver {

    private final IRoleEntityResolver roleResolver = Jdp.getRequired(IRoleEntityResolver.class);

    @Override
    protected RoleToPermissionRef resolveNestedRefs(final RoleToPermissionRef ref) {
        if (ref instanceof RoleToPermissionKey key) {
            final RoleToPermissionInternalKey inkey = new RoleToPermissionInternalKey();
            inkey.setRoleRef(roleResolver.getRef(key.getRoleRef()));
            inkey.setPermissionId(key.getPermissionId());
            inkey.setTenantId(getSharedTenantId());
            return inkey;
        }
        return super.resolveNestedRefs(ref);
    }
}
