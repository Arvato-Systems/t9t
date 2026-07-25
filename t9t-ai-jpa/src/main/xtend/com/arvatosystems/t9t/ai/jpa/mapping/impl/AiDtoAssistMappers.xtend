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
package com.arvatosystems.t9t.ai.jpa.mapping.impl

import com.arvatosystems.t9t.ai.AiDtoAssistDTO
import com.arvatosystems.t9t.ai.jpa.entities.AiDtoAssistEntity
import com.arvatosystems.t9t.ai.jpa.mapping.IAiAssistantDescriptionMapper
import com.arvatosystems.t9t.ai.jpa.persistence.IAiAssistantEntityResolver
import com.arvatosystems.t9t.ai.jpa.persistence.IAiDtoAssistEntityResolver
import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42

@AutoMap42
class AiDtoAssistMappers {
    IAiDtoAssistEntityResolver resolver
    IAiAssistantEntityResolver assistantResolver
    IAiAssistantDescriptionMapper assistantMapper

    @AutoHandler("P42")
    def void e2dAiDtoAssistDTO(AiDtoAssistEntity it, AiDtoAssistDTO dto) {
        dto.aiAssistantRef = assistantMapper.mapToDto(aiAssistantRef)
    }

    def void d2eAiDtoAssistDTO(AiDtoAssistEntity entity, AiDtoAssistDTO it) {
        entity.aiAssistantRef = assistantResolver.getRef(aiAssistantRef)
    }
}
