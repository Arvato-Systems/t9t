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
package com.arvatosystems.t9t.io.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.io.AsyncMessageDTO
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity
import com.arvatosystems.t9t.io.jpa.mapping.IAsyncQueueDTOMapper
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver

@AutoMap42
class AsyncMessageMappers {
    IAsyncMessageEntityResolver resolver
    IAsyncQueueEntityResolver queueResolver
    IAsyncQueueDTOMapper queueMapper

    @AutoHandler("SC42")
    def void e2dAsyncMessageDTO(AsyncMessageEntity entity, AsyncMessageDTO dto) {
        if (entity.lastAttempt !== null && entity.whenSent !== null)
            dto.latency = entity.lastAttempt.millis - entity.whenSent.millis
        dto.asyncQueueRef = queueMapper.mapToDto(entity.asyncQueue)
    }
    def void d2eAsyncMessageDTO(AsyncMessageEntity entity, AsyncMessageDTO dto, boolean onlyActive) {
        entity.asyncQueueRef = queueResolver.getRef(dto.asyncQueueRef, onlyActive)
    }
}
