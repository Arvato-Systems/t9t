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
package com.arvatosystems.t9t.ssm.be.request;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.jpa.entities.SchedulerSetupEntity;
import com.arvatosystems.t9t.ssm.jpa.mapping.ISchedulerSetupDTOMapper;
import com.arvatosystems.t9t.ssm.jpa.persistence.ISchedulerSetupEntityResolver;
import com.arvatosystems.t9t.ssm.request.SchedulerSetupSearchRequest;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;

public class SchedulerSetupSearchRequestHandler extends AbstractSearchWithTotalsRequestHandler<Long, SchedulerSetupDTO, FullTrackingWithVersion,
  SchedulerSetupSearchRequest, SchedulerSetupEntity> {
    private final ISchedulerSetupEntityResolver resolver = Jdp.getRequired(ISchedulerSetupEntityResolver.class);
    private final ISchedulerSetupDTOMapper mapper = Jdp.getRequired(ISchedulerSetupDTOMapper.class);

    @Override
    public ReadAllResponse<SchedulerSetupDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final SchedulerSetupSearchRequest request)
      throws Exception {
        final ReadAllResponse<SchedulerSetupDTO, FullTrackingWithVersion> response = this.execute(ctx, request, resolver, mapper);

        if (Boolean.TRUE.equals(request.getSuppressResponseParameters())) {
            for (final DataWithTrackingS<SchedulerSetupDTO, FullTrackingWithVersion> dwt : response.getDataList()) {
                final CannedRequestRef rq = dwt.getData().getRequest();
                if (rq != null) {
                    ((CannedRequestDTO) rq).setRequest(null);
                }
            }
        }
        return response;
    }
}
