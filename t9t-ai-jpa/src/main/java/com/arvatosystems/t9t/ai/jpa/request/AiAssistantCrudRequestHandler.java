/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiAssistantDescription;
import com.arvatosystems.t9t.ai.AiAssistantRef;
import com.arvatosystems.t9t.ai.AiSyncStatusType;
import com.arvatosystems.t9t.ai.jpa.entities.AiAssistantEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiAssistantDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiAssistantEntityResolver;
import com.arvatosystems.t9t.ai.request.AiAssistantCrudRequest;
import com.arvatosystems.t9t.ai.service.IAIChatService;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.vdb.service.IVectorIO;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;

public class AiAssistantCrudRequestHandler
    extends AbstractCrudSurrogateKeyRequestHandler<AiAssistantRef, AiAssistantDTO, FullTrackingWithVersion, AiAssistantCrudRequest, AiAssistantEntity> {

    private final IAiAssistantDTOMapper mapper = Jdp.getRequired(IAiAssistantDTOMapper.class);
    private final IAiAssistantEntityResolver resolver = Jdp.getRequired(IAiAssistantEntityResolver.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    private void validate(@Nonnull final AiAssistantDTO dto) {
        // validate that we have implementations for the specified qualifiers
        if (dto.getAiProvider() != null) {
            final IAIChatService aiImplementation = Jdp.getOptional(IAIChatService.class, dto.getAiProvider());
            if (aiImplementation == null) {
                throw new T9tException(T9tException.NO_IMPLEMENTATION_FOR_SPECIFIED_QUALIFIER,
                        IAIChatService.class.getSimpleName() + ": " + dto.getAiProvider());
            }
        }
        if (dto.getVectorDbProvider() != null) {
            final IVectorIO aiImplementation = Jdp.getOptional(IVectorIO.class, dto.getVectorDbProvider());
            if (aiImplementation == null) {
                throw new T9tException(T9tException.NO_IMPLEMENTATION_FOR_SPECIFIED_QUALIFIER,
                        IVectorIO.class.getSimpleName() + ": " + dto.getVectorDbProvider());
            }
        }
    }

    @Override
    protected void validateUpdate(final AiAssistantEntity current, final AiAssistantDTO intended) {
        validate(intended);
        intended.setSyncStatus(AiSyncStatusType.TO_BE_UPDATED);
    }

    protected void validateCreate(final AiAssistantDTO intended) {
        validate(intended);
        intended.setSyncStatus(AiSyncStatusType.TO_BE_UPDATED);
    }

    @Override
    public CrudSurrogateKeyResponse<AiAssistantDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final AiAssistantCrudRequest request) {
        final OperationType crud = request.getCrud();
        if (crud != OperationType.READ) {
            executor.clearCache(AiAssistantDescription.class.getSimpleName(), null);
        }
        final CrudSurrogateKeyResponse<AiAssistantDTO, FullTrackingWithVersion> resp = execute(ctx, mapper, resolver, request);
        // TODO: update / create at provider...
//        if (crud == OperationType.CREATE) {
//            final AiAssistantDTO dto = resp.getData();
//            final IAIChatService aiImplementation = Jdp.getRequired(IAIChatService.class, dto.getAiProvider());
//            aiImplementation.createAssistant(ctx, dto);
//        }
        return resp;
    }
}
