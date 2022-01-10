/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.voice.VoiceUserDTO;
import com.arvatosystems.t9t.voice.jpa.entities.VoiceUserEntity;
import com.arvatosystems.t9t.voice.jpa.mapping.IVoiceUserDTOMapper;
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceUserEntityResolver;
import com.arvatosystems.t9t.voice.services.IVoicePersistenceAccess;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import java.util.List;
import javax.persistence.TypedQuery;

@Singleton
public class VoicePersistenceAccess implements IVoicePersistenceAccess {
    protected final IVoiceUserEntityResolver voiceUserResolver = Jdp.getRequired(IVoiceUserEntityResolver.class);
    protected final IVoiceUserDTOMapper voiceUserMapper = Jdp.getRequired(IVoiceUserDTOMapper.class);

    @Override
    public VoiceUserDTO getUserForExternalId(final Long tenantRef, final Long applicationRef, final String providerId) {
        final TypedQuery<VoiceUserEntity> query = voiceUserResolver.getEntityManager().createQuery(
                "SELECT u FROM VoiceUserEntity u"
              + " WHERE u.tenantRef      = :tenantRef"
              + "   AND u.providerId     = :providerId"
              + "   AND u.providerIdHash = :providerIdHash"
              + "   AND u.applicationRef = :applicationRef", VoiceUserEntity.class);
        query.setParameter("tenantRef",      tenantRef);
        query.setParameter("providerId",     providerId);
        query.setParameter("providerIdHash", providerId.hashCode());
        query.setParameter("applicationRef", applicationRef);
        final List<VoiceUserEntity> results = query.getResultList();
        if (results.size() == 0) {
            return null;
        }
        return voiceUserMapper.mapToDto(results.get(0));
    }
}
