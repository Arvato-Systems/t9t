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
package com.arvatosystems.t9t.core.be.request

import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.CannedRequestRef
import com.arvatosystems.t9t.core.request.CannedRequestCrudRequest
import com.arvatosystems.t9t.core.services.ICannedRequestResolver
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class CannedRequestCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<CannedRequestRef, CannedRequestDTO, FullTrackingWithVersion, CannedRequestCrudRequest> {
    @Inject CannedRequestParameterEvaluator evaluator
    @Inject ICannedRequestResolver          resolver

    override ServiceResponse execute(RequestContext ctx, CannedRequestCrudRequest crudRequest) {
        val CannedRequestDTO dto = crudRequest.data

        switch (crudRequest.crud) {
            case CREATE, case UPDATE, case MERGE: {
                evaluator.processDTO(dto)
            }
            default: {
            }
        }
        val response = execute(ctx, crudRequest, resolver)
        if (Boolean.TRUE == crudRequest.suppressResponseParameters && response.data !== null)
            response.data.request = null
        return response
    }
}
