/*
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
package com.arvatosystems.t9t.core.jpa.request;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.jpa.entities.CannedRequestEntity;
import com.arvatosystems.t9t.core.jpa.mapping.ICannedRequestDTOMapper;
import com.arvatosystems.t9t.core.jpa.persistence.ICannedRequestEntityResolver;
import com.arvatosystems.t9t.core.request.CannedRequestSearchRequest;

import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;

public class CannedRequestSearchRequestHandler extends AbstractSearch42RequestHandler<Long, CannedRequestDTO, FullTrackingWithVersion,
  CannedRequestSearchRequest, CannedRequestEntity> {
    protected final ICannedRequestEntityResolver resolver = Jdp.getRequired(ICannedRequestEntityResolver.class);
    protected final ICannedRequestDTOMapper mapper = Jdp.getRequired(ICannedRequestDTOMapper.class);

    @Override
    public ReadAllResponse<CannedRequestDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final CannedRequestSearchRequest request) throws Exception {
        final ReadAllResponse<CannedRequestDTO, FullTrackingWithVersion> response = execute(ctx, request, resolver, mapper);

        /** Clear request, if asked to do so. */
        if (Boolean.TRUE.equals(request.getSuppressResponseParameters())) {
            for (final DataWithTrackingW<CannedRequestDTO, FullTrackingWithVersion> dwt : response.getDataList()) {
                dwt.getData().setRequest(null);
            }
        }
        return response;
    }
}
