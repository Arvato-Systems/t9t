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
package com.arvatosystems.t9t.batch.jpa.request;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.SliceTrackingInterval;
import com.arvatosystems.t9t.batch.request.GetNextTimeSliceRequest;
import com.arvatosystems.t9t.batch.request.GetNextTimeSliceResponse;
import com.arvatosystems.t9t.batch.services.IGetNextTimeSliceService;

import de.jpaw.dp.Inject;
import de.jpaw.dp.Jdp;

public class GetNextTimeSliceRequestHandler extends AbstractRequestHandler<GetNextTimeSliceRequest> {
    @Inject
    private final IGetNextTimeSliceService sliceService = Jdp.getRequired(IGetNextTimeSliceService.class);

    @Override
    public GetNextTimeSliceResponse execute(final RequestContext ctx, final GetNextTimeSliceRequest rq) {
        final SliceTrackingInterval result = sliceService.getNextTimeSlice(ctx, rq.getSliceTrackingKey().getDataSinkId(), rq.getSliceTrackingKey().getId(),
                rq.getOverrideEndInstant(), rq.getSinkRef());

        final GetNextTimeSliceResponse response = new GetNextTimeSliceResponse();
        response.setStartInstant(result.getStartInstant());
        response.setEndInstant(result.getEndInstant());
        return response;
    }
}
