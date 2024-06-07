package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThread;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIThreadResponse;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIGetThreadByIdRequest;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAIGetThreadByIdRequestHandler extends AbstractRequestHandler<OpenAIGetThreadByIdRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIThreadResponse execute(final RequestContext ctx, final OpenAIGetThreadByIdRequest request) throws Exception {

        final OpenAIThread assistant = openAIClient.getThreadById(request.getId());
        final OpenAIThreadResponse resp = new OpenAIThreadResponse();
        resp.setThread(assistant);
        return resp;
    }
}
