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
package com.arvatosystems.t9t.ai.jpa.request;

import com.arvatosystems.t9t.ai.AiConversationDTO;
import com.arvatosystems.t9t.ai.AiConversationRef;
import com.arvatosystems.t9t.ai.jpa.entities.AiConversationEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiConversationDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiConversationEntityResolver;
import com.arvatosystems.t9t.ai.request.AiConversationCrudRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class AiConversationCrudRequestHandler extends AbstractCrudSurrogateKeyRequestHandler<AiConversationRef, AiConversationDTO,
  FullTrackingWithVersion, AiConversationCrudRequest, AiConversationEntity> {

    private final IAiConversationDTOMapper mapper = Jdp.getRequired(IAiConversationDTOMapper.class);
    private final IAiConversationEntityResolver resolver = Jdp.getRequired(IAiConversationEntityResolver.class);

    @Override
    public CrudSurrogateKeyResponse<AiConversationDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final AiConversationCrudRequest request) {
        final CrudSurrogateKeyResponse<AiConversationDTO, FullTrackingWithVersion> resp = execute(ctx, mapper, resolver, request);
        return resp;
    }
}
