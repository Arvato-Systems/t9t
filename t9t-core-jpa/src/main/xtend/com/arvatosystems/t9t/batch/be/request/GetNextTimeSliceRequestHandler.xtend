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
package com.arvatosystems.t9t.batch.be.request

import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.batch.jpa.persistence.ISliceTrackingEntityResolver
import com.arvatosystems.t9t.batch.request.GetNextTimeSliceRequest
import com.arvatosystems.t9t.batch.request.GetNextTimeSliceResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class GetNextTimeSliceRequestHandler extends AbstractRequestHandler<GetNextTimeSliceRequest> {
    @Inject ISliceTrackingEntityResolver resolver

    override GetNextTimeSliceResponse execute(RequestContext ctx, GetNextTimeSliceRequest rq) {

        //should we search for available timeSlice, if none is available, create a new one
        var slice = resolver.findByDataSinkIdAndId(false, rq.sliceTrackingKey.dataSinkId, rq.sliceTrackingKey.id)
        if (slice === null) {
            slice = resolver.newEntityInstance
            slice => [
                id = rq.sliceTrackingKey.id
                dataSinkId = rq.sliceTrackingKey.dataSinkId
                exportedDataBefore = ctx.executionStart
            ]
            resolver.save(slice)
        }

//        val slice                   = resolver.getEntityData(rq.sliceTrackingKey, false)
        val response                = new GetNextTimeSliceResponse
        if (LOGGER.isDebugEnabled) {
            LOGGER.info("Advancing time slice for {} from {} to {} (by {} seconds)", rq.sliceTrackingKey,
                slice.exportedDataBefore, rq.endInstant,
                (rq.endInstant.millis - slice.exportedDataBefore.millis) / 1000L
            )
        }
        response.startInstant       = slice.exportedDataBefore
        slice.exportedDataBefore    = rq.endInstant
        slice.lastSinkRef           = rq.sinkRef
        return response
    }
}
