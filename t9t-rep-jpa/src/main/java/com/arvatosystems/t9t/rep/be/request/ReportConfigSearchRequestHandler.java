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
package com.arvatosystems.t9t.rep.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.be.request.restriction.IReportConfigByUserPermissionRestriction;
import com.arvatosystems.t9t.rep.jpa.entities.ReportConfigEntity;
import com.arvatosystems.t9t.rep.jpa.mapping.IReportConfigDTOMapper;
import com.arvatosystems.t9t.rep.jpa.persistence.IReportConfigEntityResolver;
import com.arvatosystems.t9t.rep.request.ReportConfigSearchRequest;

import de.jpaw.dp.Jdp;

public class ReportConfigSearchRequestHandler extends AbstractSearchWithTotalsRequestHandler<Long, ReportConfigDTO, FullTrackingWithVersion,
  ReportConfigSearchRequest, ReportConfigEntity> {
    private final IReportConfigEntityResolver resolver = Jdp.getRequired(IReportConfigEntityResolver.class);
    private final IReportConfigDTOMapper mapper = Jdp.getRequired(IReportConfigDTOMapper.class);
    private final IReportConfigByUserPermissionRestriction reportConfigByUserPermissionRestriction
      = Jdp.getRequired(IReportConfigByUserPermissionRestriction.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ReportConfigSearchRequest request) throws Exception {
        reportConfigByUserPermissionRestriction.apply(ctx, request);
        return execute(ctx, request, resolver, mapper);
    }
}
