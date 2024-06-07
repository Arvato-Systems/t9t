package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThread;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIThreadResponse;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAICreateThreadRequest;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAICreateThreadRequestHandler extends AbstractRequestHandler<OpenAICreateThreadRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIThreadResponse execute(final RequestContext ctx, final OpenAICreateThreadRequest request) throws Exception {

        final OpenAIThread assistant = openAIClient.createThread();
        final OpenAIThreadResponse resp = new OpenAIThreadResponse();
        resp.setThread(assistant);
        return resp;
    }
}
