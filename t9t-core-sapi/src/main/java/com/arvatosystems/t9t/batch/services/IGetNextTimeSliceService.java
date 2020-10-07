package com.arvatosystems.t9t.batch.services;

import org.joda.time.Instant;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.SliceTrackingInterval;
import com.arvatosystems.t9t.batch.SliceTrackingLocalInterval;

public interface IGetNextTimeSliceService {
    /**
     * Advances the slice tracking entry identified by the key (dataSinkId, id).
     * All parameters are not nullable, except the optional overrideAsOf and sinkRef.
     * If overrideAsOf is null, ctx.executionStart will be used, minus a configurable gap.
     * If overrideAsOf is not null, it will be used as the precise end instant. It is the responsibility of the caller to ensure that it is rounded to full seconds.
     */
    SliceTrackingInterval getNextTimeSlice(RequestContext ctx, String dataSinkId, String id, Instant overrideAsOf, Long sinkRef);

    /** Retrieves the start date of the next slice for a given key, without storing anything. */
    SliceTrackingInterval previewNextTimeSlice(RequestContext ctx, String dataSinkId, String id);

    /** Converts the interval with instants to an interval of LocalDateTime types - in UTC. */
    default SliceTrackingLocalInterval convertToLocal(SliceTrackingInterval interval) {
        return new SliceTrackingLocalInterval(interval.getStartInstant().toDateTime().toLocalDateTime(), interval.getEndInstant().toDateTime().toLocalDateTime());
    }
}
