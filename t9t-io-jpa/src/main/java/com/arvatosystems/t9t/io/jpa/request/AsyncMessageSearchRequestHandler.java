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

import com.arvatosystems.t9t.base.entities.WriteTracking;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncMessageDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IAsyncMessageDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncMessageSearchRequest;

import de.jpaw.dp.Jdp;

public class AsyncMessageSearchRequestHandler extends
        AbstractSearch42RequestHandler<Long, AsyncMessageDTO, WriteTracking, AsyncMessageSearchRequest, AsyncMessageEntity> {

    protected final IAsyncMessageEntityResolver resolver = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    protected final IAsyncMessageDTOMapper mapper = Jdp.getRequired(IAsyncMessageDTOMapper.class);

    @Override
    public ReadAllResponse<AsyncMessageDTO, WriteTracking> execute(final RequestContext ctx,
            final AsyncMessageSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
