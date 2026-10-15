package com.arvatosystems.t9t.refsw.impl;

import com.arvatosystems.t9t.api.ReadWriteTracking;
import com.arvatosystems.t9t.refsw.RequestContext;
import com.arvatosystems.t9t.refsw.TrackingUpdater;

public class ReadWriteTrackingUpdater implements TrackingUpdater<ReadWriteTracking> {

    @Override
    public void preCreate(RequestContext ctx, ReadWriteTracking tr) {
        tr.setWhenCreated(ctx.getExecutionStart());
    }

    @Override
    public void preUpdate(RequestContext ctx, ReadWriteTracking tr) {
        tr.setWhenUpdated(ctx.getExecutionStart());
    }
}
