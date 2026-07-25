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

import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.ai.AiDtoAssistDTO;
import com.arvatosystems.t9t.ai.jpa.entities.AiDtoAssistEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiDtoAssistDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiDtoAssistEntityResolver;
import com.arvatosystems.t9t.ai.request.AiDtoAssistSearchRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

public class AiDtoAssistSearchRequestHandler
    extends AbstractSearchWithTotalsRequestHandler<Long, AiDtoAssistDTO, FullTrackingWithVersion, AiDtoAssistSearchRequest, AiDtoAssistEntity> {

    private final IAiDtoAssistEntityResolver resolver = Jdp.getRequired(IAiDtoAssistEntityResolver.class);
    private final IAiDtoAssistDTOMapper mapper = Jdp.getRequired(IAiDtoAssistDTOMapper.class);

    @Override
    public ReadAllResponse<AiDtoAssistDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final AiDtoAssistSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
