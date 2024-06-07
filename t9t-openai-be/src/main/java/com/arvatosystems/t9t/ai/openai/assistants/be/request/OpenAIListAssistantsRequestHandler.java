package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectAssistant;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIListAssistantsRequest;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIListResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

public class OpenAIListAssistantsRequestHandler extends AbstractOpenAIListRequestHandler<OpenAIObjectAssistant, OpenAIListAssistantsRequest> {

    @Override
    public OpenAIListResponse<OpenAIObjectAssistant> execute(final RequestContext ctx, final OpenAIListAssistantsRequest request) throws Exception {
        return createResult(openAIClient.listAssistants(request.getQueryParameters()));
    }
}
