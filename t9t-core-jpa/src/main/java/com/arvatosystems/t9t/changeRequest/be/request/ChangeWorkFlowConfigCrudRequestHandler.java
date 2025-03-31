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
package com.arvatosystems.t9t.changeRequest.be.request;

import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigRef;
import com.arvatosystems.t9t.changeRequest.jpa.entities.ChangeWorkFlowConfigEntity;
import com.arvatosystems.t9t.changeRequest.jpa.mapping.IChangeWorkFlowConfigDTOMapper;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IChangeWorkFlowConfigEntityResolver;
import com.arvatosystems.t9t.changeRequest.request.ChangeWorkFlowConfigCrudRequest;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;

public class ChangeWorkFlowConfigCrudRequestHandler extends AbstractCrudSurrogateKeyRequestHandler<ChangeWorkFlowConfigRef, ChangeWorkFlowConfigDTO,
    FullTrackingWithVersion, ChangeWorkFlowConfigCrudRequest, ChangeWorkFlowConfigEntity> {

    private final IChangeWorkFlowConfigEntityResolver resolver = Jdp.getRequired(IChangeWorkFlowConfigEntityResolver.class);
    private final IChangeWorkFlowConfigDTOMapper mapper = Jdp.getRequired(IChangeWorkFlowConfigDTOMapper.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Nonnull
    @Override
    public CrudSurrogateKeyResponse<ChangeWorkFlowConfigDTO, FullTrackingWithVersion> execute(@Nonnull final RequestContext ctx,
        @Nonnull final ChangeWorkFlowConfigCrudRequest request) throws Exception {
        if (request.getCrud() != OperationType.READ) {
            executor.clearCache(ChangeWorkFlowConfigDTO.class.getSimpleName(), null);
        }
        return execute(ctx, mapper, resolver, request);
    }
}
