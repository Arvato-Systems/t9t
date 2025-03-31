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

import com.arvatosystems.t9t.ai.AiModuleCfgDTO;
import com.arvatosystems.t9t.ai.jpa.entities.AiModuleCfgEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiModuleCfgDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiModuleCfgEntityResolver;
import com.arvatosystems.t9t.ai.request.AiModuleCfgSearchRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class AiModuleCfgSearchRequestHandler extends
  AbstractSearchWithTotalsRequestHandler<String, AiModuleCfgDTO, FullTrackingWithVersion, AiModuleCfgSearchRequest, AiModuleCfgEntity> {

    private final IAiModuleCfgEntityResolver resolver = Jdp.getRequired(IAiModuleCfgEntityResolver.class);
    private final IAiModuleCfgDTOMapper mapper = Jdp.getRequired(IAiModuleCfgDTOMapper.class);

    @Override
    public ReadAllResponse<AiModuleCfgDTO, FullTrackingWithVersion>
      execute(final RequestContext ctx, final AiModuleCfgSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
