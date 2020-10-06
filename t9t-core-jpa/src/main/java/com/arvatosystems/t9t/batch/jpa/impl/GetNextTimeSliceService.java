package com.arvatosystems.t9t.batch.jpa.impl;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.SliceTrackingInterval;
import com.arvatosystems.t9t.batch.jpa.entities.SliceTrackingEntity;
import com.arvatosystems.t9t.batch.jpa.persistence.ISliceTrackingEntityResolver;
import com.arvatosystems.t9t.batch.services.IGetNextTimeSliceService;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class GetNextTimeSliceService implements IGetNextTimeSliceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetNextTimeSliceService.class);
    private static final Integer DEFAULT_GAP_IN_SECONDS = 10;
    private static final long DEFAULT_GAP_IN_MILLIS = DEFAULT_GAP_IN_SECONDS * 1000L;
    private static final Instant REALLY_LONG_AGO = new Instant(1L); // somewhere 1970

    protected final ISliceTrackingEntityResolver resolver = Jdp.getRequired(ISliceTrackingEntityResolver.class);

    @Override
    public SliceTrackingInterval getNextTimeSlice(RequestContext ctx, String dataSinkId, String id, Instant overrideAsOf, Long sinkRef) {
        final Instant endInstantPrecise = overrideAsOf != null ? overrideAsOf : ctx.executionStart;
        final Instant endInstantRoundedDown = endInstantPrecise.minus(endInstantPrecise.getMillis() % 1000L);

        final SliceTrackingInterval result = new SliceTrackingInterval();
        // we search for an existing timeSlice, if none is available, create a new one
        SliceTrackingEntity slice = resolver.findByDataSinkIdAndId(false, dataSinkId, id);
        if (slice == null) {
            result.setStartInstant(REALLY_LONG_AGO);
            final Instant endInstantRoundedDownMinusGap = endInstantRoundedDown.minus(DEFAULT_GAP_IN_MILLIS);
            slice = resolver.newEntityInstance();
            slice.setDataSinkId(dataSinkId);
            slice.setId(id);
            slice.setExportedDataBefore(endInstantRoundedDownMinusGap);
            slice.setLastSinkRef(sinkRef);
            resolver.save(slice);
            LOGGER.info("Creating NEW time slice for {}/{} to {}", dataSinkId, id, endInstantRoundedDownMinusGap);
        } else {
            result.setStartInstant(slice.getExportedDataBefore());
            final Integer gap = slice.getGap() != null ? slice.getGap() : DEFAULT_GAP_IN_SECONDS;
            final Instant endInstantRoundedDownMinusGap = endInstantRoundedDown.minus(gap * 1000L);
            LOGGER.debug("Advancing time slice for {}/{} from {} to {} (by {} seconds)", dataSinkId, id, slice.getExportedDataBefore(), endInstantRoundedDownMinusGap, gap);
            slice.setExportedDataBefore(endInstantRoundedDownMinusGap);
            slice.setLastSinkRef(sinkRef);
        }
        result.setEndInstant(slice.getExportedDataBefore());
        return result;
    }
}
