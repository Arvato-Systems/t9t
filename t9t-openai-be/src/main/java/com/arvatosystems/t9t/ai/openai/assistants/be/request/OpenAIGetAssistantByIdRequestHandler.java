package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectAssistant;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIAssistantResponse;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIGetAssistantByIdRequest;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAIGetAssistantByIdRequestHandler extends AbstractRequestHandler<OpenAIGetAssistantByIdRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIAssistantResponse execute(final RequestContext ctx, final OpenAIGetAssistantByIdRequest request) throws Exception {

        final OpenAIObjectAssistant assistant = openAIClient.getAssistantById(request.getId());
        final OpenAIAssistantResponse resp = new OpenAIAssistantResponse();
        resp.setAssistant(assistant);
        return resp;
    }
}
