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
package com.arvatosystems.t9t.voice.be.request;

import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.voice.VoiceUserDTO;
import com.arvatosystems.t9t.voice.VoiceUserRef;
import com.arvatosystems.t9t.voice.jpa.entities.VoiceUserEntity;
import com.arvatosystems.t9t.voice.jpa.mapping.IVoiceUserDTOMapper;
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceUserEntityResolver;
import com.arvatosystems.t9t.voice.request.VoiceUserCrudRequest;
import de.jpaw.dp.Jdp;

public class VoiceUserCrudRequestHandler extends
        AbstractCrudSurrogateKey42RequestHandler<VoiceUserRef, VoiceUserDTO, FullTrackingWithVersion, VoiceUserCrudRequest, VoiceUserEntity> {
    protected final IVoiceUserEntityResolver resolver = Jdp.getRequired(IVoiceUserEntityResolver.class);

    protected final IVoiceUserDTOMapper mapper = Jdp.getRequired(IVoiceUserDTOMapper.class);

    @Override
    public CrudSurrogateKeyResponse<VoiceUserDTO, FullTrackingWithVersion> execute(final VoiceUserCrudRequest request)
            throws Exception {
        // if a DTO is provided, set the hash code as required
        VoiceUserDTO dto = request.getData();
        if (dto != null)
            dto.setProviderIdHash(dto.getProviderId().hashCode());
        return execute(mapper, resolver, request);
    }
}
