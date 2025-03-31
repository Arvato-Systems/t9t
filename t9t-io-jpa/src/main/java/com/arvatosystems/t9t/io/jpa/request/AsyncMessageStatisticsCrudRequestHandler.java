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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncMessageStatisticsDTO;
import com.arvatosystems.t9t.io.AsyncMessageStatisticsRef;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageStatisticsEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IAsyncMessageStatisticsDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageStatisticsEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncMessageStatisticsCrudRequest;

import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.dp.Jdp;

public class AsyncMessageStatisticsCrudRequestHandler extends
    AbstractCrudSurrogateKeyRequestHandler<AsyncMessageStatisticsRef, AsyncMessageStatisticsDTO, NoTracking, AsyncMessageStatisticsCrudRequest,
    AsyncMessageStatisticsEntity> {

    private final IAsyncMessageStatisticsEntityResolver resolver = Jdp.getRequired(IAsyncMessageStatisticsEntityResolver.class);
    private final IAsyncMessageStatisticsDTOMapper mapper = Jdp.getRequired(IAsyncMessageStatisticsDTOMapper.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final AsyncMessageStatisticsCrudRequest request) throws Exception {
        return execute(ctx, mapper, resolver, request);
    }

}
