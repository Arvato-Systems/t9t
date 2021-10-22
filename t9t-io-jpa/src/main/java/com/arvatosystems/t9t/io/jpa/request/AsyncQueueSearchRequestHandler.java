/**
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IAsyncQueueDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncQueueSearchRequest;
import de.jpaw.dp.Jdp;

public class AsyncQueueSearchRequestHandler extends
        AbstractSearch42RequestHandler<Long, AsyncQueueDTO, FullTrackingWithVersion, AsyncQueueSearchRequest, AsyncQueueEntity> {

    protected final IAsyncQueueEntityResolver resolver = Jdp.getRequired(IAsyncQueueEntityResolver.class);
    protected final IAsyncQueueDTOMapper mapper = Jdp.getRequired(IAsyncQueueDTOMapper.class);

    @Override
    public ReadAllResponse<AsyncQueueDTO, FullTrackingWithVersion> execute(final RequestContext ctx,
            final AsyncQueueSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
