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
package com.arvatosystems.t9t.trns.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudModuleCfgRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.trns.TrnsModuleCfgDTO;
import com.arvatosystems.t9t.trns.jpa.entities.TrnsModuleCfgEntity;
import com.arvatosystems.t9t.trns.jpa.mapping.ITrnsModuleCfgDTOMapper;
import com.arvatosystems.t9t.trns.jpa.persistence.ITrnsModuleCfgEntityResolver;
import com.arvatosystems.t9t.trns.request.TrnsModuleCfgCrudRequest;

import de.jpaw.dp.Jdp;

public class TrnsModuleCfgCrudRequestHandler extends AbstractCrudModuleCfgRequestHandler<TrnsModuleCfgDTO, TrnsModuleCfgCrudRequest, TrnsModuleCfgEntity> {

    private final ITrnsModuleCfgDTOMapper mapper = Jdp.getRequired(ITrnsModuleCfgDTOMapper.class);
    private final ITrnsModuleCfgEntityResolver resolver = Jdp.getRequired(ITrnsModuleCfgEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final TrnsModuleCfgCrudRequest request) throws Exception {
        return execute(ctx, mapper, resolver, request);
    }
}
