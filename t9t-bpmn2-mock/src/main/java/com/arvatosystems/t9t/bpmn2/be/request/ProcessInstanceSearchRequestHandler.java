package com.arvatosystems.t9t.bpmn2.be.request;

import java.util.Collections;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.ProcessInstanceDTO;
import com.arvatosystems.t9t.bpmn2.request.ProcessInstanceSearchRequest;

public class ProcessInstanceSearchRequestHandler extends AbstractRequestHandler<ProcessInstanceSearchRequest> {

    @Override
    public ServiceResponse execute(RequestContext ctx, ProcessInstanceSearchRequest request) throws Exception {
        final ReadAllResponse<ProcessInstanceDTO, FullTrackingWithVersion> response = new ReadAllResponse<>();
        response.setDataList(Collections.emptyList());
        response.setNumResults(0l);
        return response;
    }
}
