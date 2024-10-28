/*
 * Copyright (c) 2012 - 2024 Arvato Systems GmbH
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

import com.arvatosystems.t9t.ai.AiChatLogDTO
import com.arvatosystems.t9t.ai.jpa.entities.AiChatLogEntity
import com.arvatosystems.t9t.ai.jpa.mapping.IAiConversationDTOMapper
import com.arvatosystems.t9t.ai.jpa.persistence.IAiChatLogEntityResolver
import com.arvatosystems.t9t.ai.jpa.persistence.IAiConversationEntityResolver
import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42

@AutoMap42
class AiChatLogMappers {
    IAiChatLogEntityResolver resolver
    IAiConversationEntityResolver conversationResolver
    IAiConversationDTOMapper conversationDTOMapper

    @AutoHandler("S")
    def void e2dAiChatLogDTO(AiChatLogEntity it, AiChatLogDTO dto) {
        dto.conversationRef  = conversationDTOMapper.mapToDto(conversationRef)
    }
    def void d2eAiChatLogDTO(AiChatLogEntity entity, AiChatLogDTO it) {
        entity.conversationRef = conversationResolver.getRef(conversationRef)
    }
}
