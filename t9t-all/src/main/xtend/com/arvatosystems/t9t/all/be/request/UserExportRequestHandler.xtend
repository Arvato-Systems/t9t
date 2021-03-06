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
package com.arvatosystems.t9t.all.be.request

import com.arvatosystems.t9t.all.request.UserExportRequest
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.jpa.mapping.IUserDTOMapper
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver
import com.arvatosystems.t9t.auth.request.UserSearchRequest
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.base.search.SinkCreatedResponse
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IExporterTool
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.SearchFilters
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.dp.Inject

@AddLogger
class UserExportRequestHandler extends AbstractRequestHandler<UserExportRequest>{

    @Inject IUserEntityResolver resolver
    @Inject IUserDTOMapper mapper
    @Inject IExporterTool<UserDTO, FullTrackingWithVersion> exporter

    static final String DATA_SINK_ID = "xmlUserExport"

    override SinkCreatedResponse execute(RequestContext ctx, UserExportRequest rq) {
        // create a search filter which contains the tenant
        val tenantFilter = new LongFilter(T9tConstants.TENANT_REF_FIELD_NAME)
        tenantFilter.equalsValue = ctx.tenantRef
        val myFilter = SearchFilters.and(tenantFilter, rq.searchFilter)

        val queryParams = new UserSearchRequest => [
            searchFilter  = myFilter
            limit         = rq.limit
            offset        = rq.offset
            sortColumns   = rq.sortColumns
        ]

        // retrieve the data
        val userDTOs = mapper.mapListToDwt(resolver.search(queryParams))
        val count = userDTOs.size

        // check if we got a sink, if not, use the default
        val sessionParams = rq.searchOutputTarget ?: (new OutputSessionParameters() => [
            dataSinkId = DATA_SINK_ID
        ])
        // export the data
        val mySinkRef = exporter.storeAll(sessionParams, userDTOs, rq.maxRecords)

        val response = new SinkCreatedResponse => [
            sinkRef    = mySinkRef
            numResults = Long.valueOf(count)
        ]
        response
    }
}
