package com.arvatosystems.t9t.refsp.impl;

import com.arvatosystems.t9t.api.WriteTracking;
import com.arvatosystems.t9t.refsp.RequestContext;
import com.arvatosystems.t9t.refsp.TrackingUpdater;

public class WriteTrackingUpdater implements TrackingUpdater<WriteTracking> {

    @Override
    public void preCreate(RequestContext ctx, WriteTracking tr) {
        tr.setWhenCreated(ctx.getExecutionStart());
    }

    @Override
    public void preUpdate(RequestContext ctx, WriteTracking tr) {
        // no changes upon update
    }
}
