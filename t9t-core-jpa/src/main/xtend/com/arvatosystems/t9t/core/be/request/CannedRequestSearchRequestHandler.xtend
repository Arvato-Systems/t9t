/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.core.be.request;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.jpa.mapping.ICannedRequestDTOMapper
import com.arvatosystems.t9t.core.jpa.persistence.ICannedRequestEntityResolver
import com.arvatosystems.t9t.core.request.CannedRequestSearchRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
public class CannedRequestSearchRequestHandler extends AbstractSearchRequestHandler<CannedRequestSearchRequest> {
    @Inject protected final ICannedRequestEntityResolver resolver

    @Inject protected final ICannedRequestDTOMapper mapper

    override public ReadAllResponse<CannedRequestDTO, FullTrackingWithVersion> execute(CannedRequestSearchRequest request) throws Exception {
        val response = mapper.createReadAllResponse(resolver.search(request, null), request.getSearchOutputTarget());
        if (Boolean.TRUE == request.suppressResponseParameters) {
              // null out explicit request parameters
              for (dwt : response.dataList)
                  dwt.data.request = null
        }
        return response
    }
}
