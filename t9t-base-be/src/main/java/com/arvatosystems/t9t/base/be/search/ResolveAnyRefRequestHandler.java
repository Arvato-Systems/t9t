package com.arvatosystems.t9t.base.be.search;

import com.arvatosystems.t9t.base.search.ResolveAnyRefRequest;
import com.arvatosystems.t9t.base.search.ResolveAnyRefResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.IAnyKeySearchRegistry;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class ResolveAnyRefRequestHandler extends AbstractReadOnlyRequestHandler<ResolveAnyRefRequest> {
    protected final IAnyKeySearchRegistry searchRegistry = Jdp.getRequired(IAnyKeySearchRegistry.class);

    @Override
    public ResolveAnyRefResponse execute(RequestContext ctx, ResolveAnyRefRequest request) throws Exception {
        return searchRegistry.performLookup(ctx, request.getRef());
    }
}
