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
package com.arvatosystems.t9t.base.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.AbstractAggregationRequest;
import com.arvatosystems.t9t.base.request.AggregationGranularityType;
import com.arvatosystems.t9t.base.request.AggregationResponse;

public abstract class AbstractAggregationRequestHandler<REQUEST extends AbstractAggregationRequest> extends AbstractRequestHandler<REQUEST> {

    @Override
    public ServiceResponse execute(final RequestContext ctx, final REQUEST request) throws Exception {
        // determine effective time range
        final AggregationGranularityType precision = T9tUtil.nvl(request.getAggregationGranularity(), AggregationGranularityType.HOUR);
        final LocalDateTime effectiveIntervalEnd = T9tUtil.truncate(T9tUtil.nvl(request.getEndExcluding(), LocalDateTime.now()), precision);
        final LocalDateTime effectiveIntervalStart = request.getStartIncluding() != null ? T9tUtil.truncate(request.getStartIncluding(), precision)
            : T9tUtil.minusDuration(effectiveIntervalEnd, precision, 1L);
        final Instant effectiveStart = effectiveIntervalStart.toInstant(ZoneOffset.UTC);
        final Instant effectiveEnd = effectiveIntervalEnd.toInstant(ZoneOffset.UTC);
        final AggregationResponse response = new AggregationResponse();
        response.setStartIncluding(effectiveIntervalStart);
        response.setEndExcluding(effectiveIntervalEnd);

        // Delete existing aggregation
        final long start = System.currentTimeMillis();
        response.setRecordsDeleted(deleteAggregation(effectiveStart, effectiveEnd));
        final long mid = System.currentTimeMillis();
        response.setMillisecondsUsedForDeletion(mid - start);

        if (!Boolean.TRUE.equals(request.getOnlyDelete())) {
            // create new aggregation
            response.setRecordsCreated(createAggregation(effectiveStart, effectiveEnd, precision));
            final long end = System.currentTimeMillis();
            response.setMillisecondsUsedForCreation(end - mid);
        }

        return response;
    }

    protected abstract int deleteAggregation(Instant fromDate, Instant toDate);

    protected abstract int createAggregation(Instant fromDate, Instant toDate, AggregationGranularityType precision);
}
