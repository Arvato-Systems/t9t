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
import com.arvatosystems.t9t.voice.VoiceApplicationDTO
import com.arvatosystems.t9t.voice.jpa.entities.VoiceApplicationEntity
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceApplicationEntityResolver

@AutoMap42
class VoiceApplicationEntityMapper {
    IVoiceApplicationEntityResolver entityResolver

    @AutoHandler("CSP42")
    def void d2eVoiceApplicationDTO(VoiceApplicationEntity entity, VoiceApplicationDTO dto, boolean onlyActive) {}
    def void e2dVoiceApplicationDTO(VoiceApplicationEntity entity, VoiceApplicationDTO dto) {}

}
