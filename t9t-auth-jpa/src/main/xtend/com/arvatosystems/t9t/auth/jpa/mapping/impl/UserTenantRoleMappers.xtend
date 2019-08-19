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
package com.arvatosystems.t9t.auth.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.auth.UserTenantRoleDTO
import com.arvatosystems.t9t.auth.jpa.entities.UserTenantRoleEntity
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleDTOMapper
import com.arvatosystems.t9t.auth.jpa.mapping.IUserDescriptionMapper
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserTenantRoleEntityResolver

@AutoMap42
class UserTenantRoleMappers {
    IUserTenantRoleEntityResolver entityResolver
    IUserDescriptionMapper userMapper
    IRoleDTOMapper roleMapper
    IUserEntityResolver userResolver
    IRoleEntityResolver roleResolver

    def void d2eUserTenantRoleDTO(UserTenantRoleEntity entity, UserTenantRoleDTO it, boolean onlyActive) {
        entity.userRef = userResolver.getRef(userRef, onlyActive)
        entity.roleRef = roleResolver.getRef(roleRef, onlyActive)
    }

    @AutoHandler("P42")
    def void e2dUserTenantRoleDTO(UserTenantRoleEntity it, UserTenantRoleDTO dto) {
        dto.userRef = userMapper.mapToDto(userRef)
        dto.roleRef = roleMapper.mapToDto(roleRef)
    }
}
