package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadMessage;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIListResponse;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIListThreadMessagesRequest;
import com.arvatosystems.t9t.base.services.RequestContext;

public class OpenAIListThreadMessagesRequestHandler extends AbstractOpenAIListRequestHandler<OpenAIObjectThreadMessage, OpenAIListThreadMessagesRequest> {

    @Override
    public OpenAIListResponse<OpenAIObjectThreadMessage> execute(final RequestContext ctx, final OpenAIListThreadMessagesRequest request) throws Exception {
        return createResult(openAIClient.listThreadMessages(request.getThreadId(), request.getQueryParameters()));
    }
}
