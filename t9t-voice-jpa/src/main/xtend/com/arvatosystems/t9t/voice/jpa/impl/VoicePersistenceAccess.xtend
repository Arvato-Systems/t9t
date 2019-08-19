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
package com.arvatosystems.t9t.voice.jpa.impl;

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.voice.jpa.entities.VoiceUserEntity
import com.arvatosystems.t9t.voice.jpa.mapping.IVoiceUserDTOMapper
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceUserEntityResolver
import com.arvatosystems.t9t.voice.services.IVoicePersistenceAccess
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton

@Singleton
@AddLogger
public class VoicePersistenceAccess implements IVoicePersistenceAccess, T9tConstants {
    @Inject protected IVoiceUserEntityResolver voiceUserResolver
    @Inject protected IVoiceUserDTOMapper      voiceUserMapper

    override getUserForExternalId(Long tenantRef, Long applicationRef, String providerId) {
        val query = voiceUserResolver.entityManager.createQuery(
            "SELECT u FROM VoiceUserEntity u" +
            " WHERE u.tenantRef      = :tenantRef" +
            "   AND u.providerId     = :providerId" +
            "   AND u.providerIdHash = :providerIdHash" +
            "   AND u.applicationRef = :applicationRef",
            VoiceUserEntity)
        query.setParameter("tenantRef",      tenantRef)
        query.setParameter("providerId",     providerId)
        query.setParameter("providerIdHash", providerId.hashCode)
        query.setParameter("applicationRef", applicationRef)

        val results = query.resultList
        return if (results.size > 0) voiceUserMapper.mapToDto(results.get(0))
    }
}
