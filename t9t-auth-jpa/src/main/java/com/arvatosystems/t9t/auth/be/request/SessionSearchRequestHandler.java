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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.SessionDTO;
import com.arvatosystems.t9t.auth.jpa.entities.SessionEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.ISessionDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.ISessionEntityResolver;
import com.arvatosystems.t9t.auth.request.SessionSearchRequest;
import com.arvatosystems.t9t.base.entities.SessionTracking;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class SessionSearchRequestHandler extends AbstractSearchWithTotalsRequestHandler<Long, SessionDTO, SessionTracking, SessionSearchRequest,
    SessionEntity> {

    protected final ISessionEntityResolver resolver = Jdp.getRequired(ISessionEntityResolver.class);
    protected final ISessionDTOMapper mapper = Jdp.getRequired(ISessionDTOMapper.class);

    @Override
    public ReadAllResponse<SessionDTO, SessionTracking> execute(final RequestContext ctx, final SessionSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
