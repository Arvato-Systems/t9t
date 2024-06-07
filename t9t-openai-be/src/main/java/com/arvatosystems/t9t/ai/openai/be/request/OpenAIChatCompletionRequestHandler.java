package com.arvatosystems.t9t.ai.openai.be.request;

import com.arvatosystems.t9t.ai.openai.OpenAIObjectChatCompletion;
import com.arvatosystems.t9t.ai.openai.request.OpenAIChatCompletionRequest;
import com.arvatosystems.t9t.ai.openai.request.OpenAIObjectChatCompletionResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAIChatCompletionRequestHandler extends AbstractRequestHandler<OpenAIChatCompletionRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIObjectChatCompletionResponse execute(final RequestContext ctx, final OpenAIChatCompletionRequest request) throws Exception {
        final OpenAIObjectChatCompletion response;
        if (request.getNumberOfToolCalls() == null) {
            response = openAIClient.performOpenAIChatCompletion(request.getRequest());
        } else {
            response = openAIClient.performOpenAIChatCompletionWithToolCalls(ctx,
              request.getRequest(), request.getToolSelection(), request.getNumberOfToolCalls());
        }
        final OpenAIObjectChatCompletionResponse resp = new OpenAIObjectChatCompletionResponse();
        resp.setResponse(response);
        return resp;
    }
}
