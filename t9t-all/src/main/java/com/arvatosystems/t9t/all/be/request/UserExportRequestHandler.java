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
package com.arvatosystems.t9t.all.be.request;

import java.util.List;

import com.arvatosystems.t9t.all.request.UserExportRequest;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.jpa.mapping.IUserDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.request.UserSearchRequest;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExporterTool;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserExportRequestHandler extends AbstractRequestHandler<UserExportRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserExportRequestHandler.class);
    private static final String DATA_SINK_ID = "xmlUserExport";

    private final IUserEntityResolver resolver = Jdp.getRequired(IUserEntityResolver.class);
    private final IUserDTOMapper mapper = Jdp.getRequired(IUserDTOMapper.class);
    @SuppressWarnings("unchecked")
    private final IExporterTool<UserDTO, FullTrackingWithVersion> exporter = Jdp.getRequired(IExporterTool.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final UserExportRequest request) throws Exception {
        // create a search filter which contains the tenant
        final LongFilter tenantFilter = new LongFilter(T9tConstants.TENANT_REF_FIELD_NAME);
        tenantFilter.setEqualsValue(ctx.getTenantRef());
        final SearchFilter myFilter = SearchFilters.and(tenantFilter, request.getSearchFilter());

        final UserSearchRequest queryParams = new UserSearchRequest();
        queryParams.setSearchFilter(myFilter);
        queryParams.setLimit(request.getLimit());
        queryParams.setOffset(request.getOffset());
        queryParams.setSortColumns(request.getSortColumns());

        // retrieve the data
        final List<DataWithTrackingW<UserDTO, FullTrackingWithVersion>> userDTOs = mapper.mapListToDwt(resolver.search(queryParams));
        final int count = userDTOs.size();

        // check if we got a sink, if not, use the default
        final OutputSessionParameters sessionParams;
        if (request.getSearchOutputTarget() == null) {
            sessionParams = new OutputSessionParameters();
            sessionParams.setDataSinkId(DATA_SINK_ID);
        } else {
            sessionParams = request.getSearchOutputTarget();
        }
        // export the data
        final Long mySinkRef = exporter.storeAll(sessionParams, userDTOs, request.getMaxRecords());

        final SinkCreatedResponse response = new SinkCreatedResponse();
        response.setSinkRef(mySinkRef);
        response.setNumResults(Long.valueOf(count));

        return response;
    }
}
