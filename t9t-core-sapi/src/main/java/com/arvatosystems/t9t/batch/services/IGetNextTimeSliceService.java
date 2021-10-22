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
package com.arvatosystems.t9t.batch.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.SliceTrackingInterval;
import com.arvatosystems.t9t.batch.SliceTrackingLocalInterval;

public interface IGetNextTimeSliceService {
    /**
     * Advances the slice tracking entry identified by the key (dataSinkId, id).
     * All parameters are not nullable, except the optional overrideAsOf and sinkRef.
     * If overrideAsOf is null, ctx.executionStart will be used, minus a configurable gap.
     * If overrideAsOf is not null, it will be used as the precise end instant.
     * It is the responsibility of the caller to ensure that it is rounded to full seconds.
     */
    SliceTrackingInterval getNextTimeSlice(RequestContext ctx, String dataSinkId, String id, Instant overrideAsOf, Long sinkRef);

    /** Retrieves the start date of the next slice for a given key, without storing anything. */
    SliceTrackingInterval previewNextTimeSlice(RequestContext ctx, String dataSinkId, String id);

    /** Converts the interval with instants to an interval of LocalDateTime types - in UTC. */
    default SliceTrackingLocalInterval convertToLocal(SliceTrackingInterval interval) {
        return new SliceTrackingLocalInterval(
            LocalDateTime.ofInstant(interval.getStartInstant(), ZoneOffset.UTC),
            LocalDateTime.ofInstant(interval.getEndInstant(), ZoneOffset.UTC));
    }
}
