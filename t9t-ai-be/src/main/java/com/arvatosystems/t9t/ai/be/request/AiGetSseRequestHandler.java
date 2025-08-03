package com.arvatosystems.t9t.ai.be.request;

import com.arvatosystems.t9t.ai.request.AiGetSseRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public class AiGetSseRequestHandler extends AbstractReadOnlyRequestHandler<AiGetSseRequest> {

    @Override
    public ServiceResponse execute(final RequestContext ctx, final AiGetSseRequest request) {
        return ok();
    }
}
