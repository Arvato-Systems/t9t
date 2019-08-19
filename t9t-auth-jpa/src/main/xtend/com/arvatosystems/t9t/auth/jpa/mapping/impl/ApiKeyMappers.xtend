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

import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.annotations.jpa.NeedMapping
import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.jpa.entities.ApiKeyEntity
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleDTOMapper
import com.arvatosystems.t9t.auth.jpa.mapping.IUserDTOMapper
import com.arvatosystems.t9t.auth.jpa.persistence.IApiKeyEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver

@AutoMap42
class ApiKeyMappers {
    IApiKeyEntityResolver   resolver
    IRoleEntityResolver     roleResolver
    IRoleDTOMapper          roleMapper
    IUserEntityResolver     userResolver
    IUserDTOMapper          userMapper

//    @AutoHandler("S42")
    @NeedMapping  // required because the DTO is final
    def void e2dApiKeyDTO(ApiKeyEntity entity, ApiKeyDTO dto) {
        dto.roleRef = roleMapper.mapToDto(entity.roleRef)
        dto.userRef = userMapper.mapToDto(entity.userRef)
    }

    @NeedMapping  // required because the DTO is final
    def void d2eApiKeyDTO(ApiKeyEntity entity, ApiKeyDTO dto, boolean onlyActive) {
        entity.roleRef = roleResolver.getRef(dto.roleRef, onlyActive)
        entity.userRef = userResolver.getRef(dto.userRef, onlyActive)
    }
}
