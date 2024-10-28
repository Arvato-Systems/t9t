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
package com.arvatosystems.t9t.auth.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.auth.RoleToPermissionDTO
import com.arvatosystems.t9t.auth.jpa.entities.RoleToPermissionEntity
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleDTOMapper
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleToPermissionEntityResolver

@AutoMap42
class RoleToPermissionMappers {
    IRoleToPermissionEntityResolver entityResolver
    IRoleDTOMapper roleMapper
    IRoleEntityResolver roleResolver

    def void d2eRoleToPermissionDTO(RoleToPermissionEntity entity, RoleToPermissionDTO it) {
        entity.roleRef = roleResolver.getRef(roleRef)
    }

    def void e2dRoleToPermissionDTO(RoleToPermissionEntity it, RoleToPermissionDTO dto) {
        dto.roleRef = roleMapper.mapToDto(roleRef)
    }
}
