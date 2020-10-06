package com.arvatosystems.t9t.batch.services;

import org.joda.time.Instant;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.SliceTrackingInterval;

public interface IGetNextTimeSliceService {
    /**
     * Advances the slice tracking entry identified by the key (dataSinkId, id).
     * All parameters are not nullable, except the optional overrideAsOf and sinkRef.
     * If overrideAsOf is null, ctx.executionStart will be used.
     */
    SliceTrackingInterval getNextTimeSlice(RequestContext ctx, String dataSinkId, String id, Instant overrideAsOf, Long sinkRef);
}
