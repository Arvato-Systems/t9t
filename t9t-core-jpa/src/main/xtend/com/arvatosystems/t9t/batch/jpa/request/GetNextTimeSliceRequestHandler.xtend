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
package com.arvatosystems.t9t.batch.jpa.request

import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.batch.request.GetNextTimeSliceRequest
import com.arvatosystems.t9t.batch.request.GetNextTimeSliceResponse
import com.arvatosystems.t9t.batch.services.IGetNextTimeSliceService
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class GetNextTimeSliceRequestHandler extends AbstractRequestHandler<GetNextTimeSliceRequest> {
    @Inject IGetNextTimeSliceService sliceService

    override GetNextTimeSliceResponse execute(RequestContext ctx, GetNextTimeSliceRequest rq) {
        val result = sliceService.getNextTimeSlice(ctx, rq.sliceTrackingKey.dataSinkId, rq.sliceTrackingKey.id, rq.overrideEndInstant, rq.sinkRef)

        val response             = new GetNextTimeSliceResponse
        response.startInstant    = result.startInstant
        response.endInstant      = result.endInstant
        return response
    }
}
