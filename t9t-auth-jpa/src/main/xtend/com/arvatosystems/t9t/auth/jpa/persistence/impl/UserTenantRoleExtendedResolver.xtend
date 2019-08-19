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

import com.arvatosystems.t9t.auth.UserTenantRoleKey
import com.arvatosystems.t9t.auth.UserTenantRoleRef
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.dp.Specializes
import com.arvatosystems.t9t.auth.UserTenantRoleInternalKey

@Specializes
@Singleton
class UserTenantRoleExtendedResolver extends UserTenantRoleEntityResolver {

    @Inject IRoleEntityResolver roleResolver;
    @Inject IUserEntityResolver userResolver;

    override protected UserTenantRoleRef resolveNestedRefs(UserTenantRoleRef ref) {
        if (ref instanceof UserTenantRoleKey) {
            return new UserTenantRoleInternalKey => [
                userRef   = userResolver.getRef(ref.userRef, false)
                roleRef   = roleResolver.getRef(ref.roleRef, false)
                tenantRef = sharedTenantRef
            ]
        }
        return super.resolveNestedRefs(ref);
    }
}
