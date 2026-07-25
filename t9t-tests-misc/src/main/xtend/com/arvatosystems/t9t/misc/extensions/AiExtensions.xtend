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
package com.arvatosystems.t9t.misc.extensions

import com.arvatosystems.t9t.ai.AiAssistantDTO
import com.arvatosystems.t9t.ai.AiAssistantKey
import com.arvatosystems.t9t.ai.AiDtoAssistDTO
import com.arvatosystems.t9t.ai.AiDtoAssistKey
import com.arvatosystems.t9t.ai.request.AiAssistantCrudRequest
import com.arvatosystems.t9t.ai.request.AiDtoAssistCrudRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import de.jpaw.bonaparte.pojos.api.OperationType

class AiExtensions {

    // extension methods for the types with surrogate keys
    def static CrudSurrogateKeyResponse<AiAssistantDTO, FullTrackingWithVersion> merge(AiAssistantDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new AiAssistantCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new AiAssistantKey(dto.assistantId)
        ], CrudSurrogateKeyResponse)
    }

    def static CrudSurrogateKeyResponse<AiDtoAssistDTO, FullTrackingWithVersion> merge(AiDtoAssistDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new AiDtoAssistCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new AiDtoAssistKey(dto.pqon)
        ], CrudSurrogateKeyResponse)
    }
}
