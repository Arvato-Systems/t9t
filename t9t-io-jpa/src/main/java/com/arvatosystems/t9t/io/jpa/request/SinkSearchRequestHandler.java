/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.ISinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.SinkSearchRequest;

import de.jpaw.dp.Jdp;

public class SinkSearchRequestHandler
        extends AbstractSearch42RequestHandler<Long, SinkDTO, FullTrackingWithVersion, SinkSearchRequest, SinkEntity> {

    protected final ISinkEntityResolver resolver = Jdp.getRequired(ISinkEntityResolver.class);
    protected final ISinkDTOMapper mapper = Jdp.getRequired(ISinkDTOMapper.class);

    @Override
    public ReadAllResponse<SinkDTO, FullTrackingWithVersion> execute(final RequestContext ctx,
            final SinkSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
