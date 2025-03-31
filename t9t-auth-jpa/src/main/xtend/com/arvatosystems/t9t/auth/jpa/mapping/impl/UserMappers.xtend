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
package com.arvatosystems.t9t.auth.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.annotations.jpa.NeedMapping
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserDescription
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleDTOMapper
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver
import com.arvatosystems.t9t.auth.UserKey

@AutoMap42
class UserMappers {
    IUserEntityResolver userResolver
    IRoleEntityResolver roleResolver
    IRoleDTOMapper roleMapper

    @NeedMapping  // required because the DTO is final
    def void d2eUserDTO(UserEntity entity, UserDTO dto) {
        entity.roleRef       = roleResolver.getRef(dto.roleRef)
        entity.supervisorRef = userResolver.getRef(dto.supervisorRef)
    }

    @NeedMapping  // required because the DTO is final
    def void e2dUserDTO(UserEntity entity, UserDTO dto) {
        dto.roleRef = roleMapper.mapToDto(entity.roleRef)
        dto.supervisorRef = if (entity.supervisor !== null) new UserKey(entity.supervisor.objectRef, entity.supervisor.userId)
    }

    def void e2dUserDescription(UserEntity entity, UserDescription dto) {
    }
}
