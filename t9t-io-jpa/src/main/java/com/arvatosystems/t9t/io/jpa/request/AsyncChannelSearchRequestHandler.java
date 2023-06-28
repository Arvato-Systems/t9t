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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncChannelEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IAsyncChannelDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncChannelEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncChannelSearchRequest;

import de.jpaw.dp.Jdp;

public class AsyncChannelSearchRequestHandler extends
        AbstractSearchWithTotalsRequestHandler<Long, AsyncChannelDTO, FullTrackingWithVersion, AsyncChannelSearchRequest, AsyncChannelEntity> {

    protected final IAsyncChannelEntityResolver resolver = Jdp.getRequired(IAsyncChannelEntityResolver.class);
    protected final IAsyncChannelDTOMapper mapper = Jdp.getRequired(IAsyncChannelDTOMapper.class);

    @Override
    public ReadAllResponse<AsyncChannelDTO, FullTrackingWithVersion> execute(final RequestContext ctx,
            final AsyncChannelSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
