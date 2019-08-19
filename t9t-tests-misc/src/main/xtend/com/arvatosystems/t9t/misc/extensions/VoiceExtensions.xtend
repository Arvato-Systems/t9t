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
package com.arvatosystems.t9t.misc.extensions

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.voice.VoiceApplicationDTO
import com.arvatosystems.t9t.voice.VoiceApplicationKey
import com.arvatosystems.t9t.voice.VoiceResponseDTO
import com.arvatosystems.t9t.voice.VoiceResponseKey
import com.arvatosystems.t9t.voice.VoiceUserDTO
import com.arvatosystems.t9t.voice.VoiceUserKey
import com.arvatosystems.t9t.voice.request.VoiceApplicationCrudRequest
import com.arvatosystems.t9t.voice.request.VoiceResponseCrudRequest
import com.arvatosystems.t9t.voice.request.VoiceUserCrudRequest
import de.jpaw.bonaparte.pojos.api.OperationType

class VoiceExtensions {
    // extension methods for the types with surrogate keys
    def static CrudSurrogateKeyResponse<VoiceApplicationDTO, FullTrackingWithVersion> merge(VoiceApplicationDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new VoiceApplicationCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new VoiceApplicationKey(dto.applicationId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<VoiceResponseDTO, FullTrackingWithVersion> merge(VoiceResponseDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new VoiceResponseCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new VoiceResponseKey(dto.applicationRef, dto.languageCode, dto.key)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<VoiceUserDTO, FullTrackingWithVersion> merge(VoiceUserDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new VoiceUserCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new VoiceUserKey(dto.applicationRef, dto.providerId)
        ], CrudSurrogateKeyResponse)
    }
}
