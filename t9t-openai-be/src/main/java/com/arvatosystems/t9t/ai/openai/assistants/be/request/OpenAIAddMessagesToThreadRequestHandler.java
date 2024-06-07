package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIAddMessagesToThreadRequest;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAIAddMessagesToThreadRequestHandler extends AbstractRequestHandler<OpenAIAddMessagesToThreadRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final OpenAIAddMessagesToThreadRequest request) throws Exception {
        openAIClient.addMessagesToThread(request.getThreadId(), request.getMessages());
        return ok();
    }
}
