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
package com.arvatosystems.t9t.ai.jpa.mapping.impl

import com.arvatosystems.t9t.ai.AiConversationDTO
import com.arvatosystems.t9t.ai.jpa.entities.AiConversationEntity
import com.arvatosystems.t9t.ai.jpa.mapping.IAiAssistantDescriptionMapper
import com.arvatosystems.t9t.ai.jpa.persistence.IAiAssistantEntityResolver
import com.arvatosystems.t9t.ai.jpa.persistence.IAiConversationEntityResolver
import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42

@AutoMap42
class AiConversationMappers {
    IAiConversationEntityResolver resolver
    IAiAssistantEntityResolver    assistantResolver
    IAiAssistantDescriptionMapper assistantMapper

    @AutoHandler("S")
    def void e2dAiConversationDTO(AiConversationEntity it, AiConversationDTO dto) {
        dto.aiAssistantRef  = assistantMapper.mapToDto(aiAssistantRef)
    }
    def void d2eAiConversationDTO(AiConversationEntity entity, AiConversationDTO it, boolean onlyActive) {
        entity.aiAssistantRef = assistantResolver.getRef(aiAssistantRef, onlyActive)
    }
}
