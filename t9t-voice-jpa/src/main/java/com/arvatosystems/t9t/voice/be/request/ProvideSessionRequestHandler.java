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
package com.arvatosystems.t9t.voice.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.voice.VoiceApplicationDTO;
import com.arvatosystems.t9t.voice.VoiceProvider;
import com.arvatosystems.t9t.voice.jpa.entities.VoiceApplicationEntity;
import com.arvatosystems.t9t.voice.jpa.mapping.IVoiceApplicationDTOMapper;
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceApplicationEntityResolver;
import com.arvatosystems.t9t.voice.request.ProvideSessionRequest;
import com.arvatosystems.t9t.voice.request.ProvideSessionResponse;
import com.arvatosystems.t9t.voice.request.VoiceApplicationSearchRequest;
import com.arvatosystems.t9t.voice.services.IVoicePersistenceAccess;
import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Jdp;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvideSessionRequestHandler extends AbstractRequestHandler<ProvideSessionRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvideSessionRequestHandler.class);

    private final IVoicePersistenceAccess persistenceAccess = Jdp.getRequired(IVoicePersistenceAccess.class);
    private final IVoiceApplicationEntityResolver applicationResolver = Jdp.getRequired(IVoiceApplicationEntityResolver.class);
    private final IVoiceApplicationDTOMapper applicationMapper = Jdp.getRequired(IVoiceApplicationDTOMapper.class);

    @Override
    public ProvideSessionResponse execute(final RequestContext ctx, final ProvideSessionRequest request) {
        // retrieve the application
        final VoiceApplicationSearchRequest appSearchRequest = new VoiceApplicationSearchRequest();
        final UnicodeFilter f1 = new UnicodeFilter("providerId", request.getApplicationId(), null, null, null, null);
        final EnumFilter f2 = new EnumFilter("provider", VoiceProvider.ALEXA.ret$PQON());
        f2.setEqualsName(request.getProvider().name());
        final AndFilter andFilter = new AndFilter(f1, f2);
        appSearchRequest.setSearchFilter(andFilter);
        final List<VoiceApplicationEntity> apps = applicationResolver.search(appSearchRequest);
        if (apps.size() != 1) {
            LOGGER.error("No voice application of ID {} found for provider {}", request.getApplicationId(), request.getProvider());
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST);
        }
        final VoiceApplicationDTO applicationDTO = applicationMapper.mapToDto(apps.get(0));
        if (!applicationDTO.getIsActive()) {
            throw new T9tException(T9tException.RECORD_INACTIVE);
        }
        final ProvideSessionResponse resp = new ProvideSessionResponse();
        resp.setTenantId(apps.get(0).getTenantId());
        resp.setApplication(applicationDTO);
        if (request.getUserId() != null) {
            // get internal user for userId
            resp.setUser(persistenceAccess.getUserForExternalId(resp.getTenantId(), applicationDTO.getObjectRef(), request.getUserId()));
        }
        return resp;
    }
}
