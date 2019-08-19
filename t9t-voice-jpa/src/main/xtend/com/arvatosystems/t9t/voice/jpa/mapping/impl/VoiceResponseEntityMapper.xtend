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
package com.arvatosystems.t9t.voice.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.voice.VoiceApplicationKey
import com.arvatosystems.t9t.voice.VoiceResponseDTO
import com.arvatosystems.t9t.voice.jpa.entities.VoiceResponseEntity
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceApplicationEntityResolver
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceResponseEntityResolver

@AutoMap42
class VoiceResponseEntityMapper {
    IVoiceResponseEntityResolver entityResolver
    IVoiceApplicationEntityResolver voiceApplicationResolver

    @AutoHandler("CSP42")
    def void d2eVoiceResponseDTO(VoiceResponseEntity entity, VoiceResponseDTO it, boolean onlyActive) {
        entity.applicationRef = voiceApplicationResolver.getRef(applicationRef, false)
    }
    def void e2dVoiceResponseDTO(VoiceResponseEntity it, VoiceResponseDTO dto) {
        val appl = voiceApplicationResolver.getEntityDataForKey(applicationRef, false)
        dto.applicationRef = new VoiceApplicationKey(appl.objectRef, appl.applicationId)
    }
}
