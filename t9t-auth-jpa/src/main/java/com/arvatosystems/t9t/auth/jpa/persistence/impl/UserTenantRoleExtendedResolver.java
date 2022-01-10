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
package com.arvatosystems.t9t.auth.jpa.persistence.impl;

import com.arvatosystems.t9t.auth.UserTenantRoleInternalKey;
import com.arvatosystems.t9t.auth.UserTenantRoleKey;
import com.arvatosystems.t9t.auth.UserTenantRoleRef;
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Specializes
@Singleton
public class UserTenantRoleExtendedResolver extends UserTenantRoleEntityResolver {

    private final IRoleEntityResolver roleResolver = Jdp.getRequired(IRoleEntityResolver.class);
    private final IUserEntityResolver userResolver = Jdp.getRequired(IUserEntityResolver.class);

    @Override
    protected UserTenantRoleRef resolveNestedRefs(final UserTenantRoleRef ref) {
        if (ref instanceof UserTenantRoleKey) {
            final UserTenantRoleKey key = (UserTenantRoleKey) ref;
            final UserTenantRoleInternalKey inkey = new UserTenantRoleInternalKey();
            inkey.setUserRef(userResolver.getRef(key.getUserRef(), false));
            inkey.setRoleRef(roleResolver.getRef(key.getRoleRef(), false));
            inkey.setTenantRef(getSharedTenantRef());
            return inkey;
        }
        return super.resolveNestedRefs(ref);
    }
}
