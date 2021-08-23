package com.arvatosystems.t9t.bpmn2.be.request;

import java.util.Collections;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn2.request.ProcessDefinitionSearchRequest;

public class ProcessDefinitionSearchRequestHandler extends AbstractRequestHandler<ProcessDefinitionSearchRequest> {

    @Override
    public ServiceResponse execute(RequestContext ctx, ProcessDefinitionSearchRequest request) throws Exception {
        final ReadAllResponse<ProcessDefinitionDTO, FullTrackingWithVersion> response = new ReadAllResponse<>();
        response.setDataList(Collections.emptyList());
        response.setNumResults(0l);
        return response;
    }

}
