package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadRun;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIListResponse;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIListThreadRunsRequest;
import com.arvatosystems.t9t.base.services.RequestContext;

public class OpenAIListThreadRunsRequestHandler extends AbstractOpenAIListRequestHandler<OpenAIObjectThreadRun, OpenAIListThreadRunsRequest> {

    @Override
    public OpenAIListResponse<OpenAIObjectThreadRun> execute(final RequestContext ctx, final OpenAIListThreadRunsRequest request) throws Exception {
        return createResult(openAIClient.listThreadRuns(request.getThreadId(), request.getQueryParameters()));
    }
}
