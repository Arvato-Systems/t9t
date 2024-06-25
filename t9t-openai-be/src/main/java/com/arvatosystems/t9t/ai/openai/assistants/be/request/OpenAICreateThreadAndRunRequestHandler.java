package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.T9tOpenAIConstants;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadRunReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadRun;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadMessages;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAICreateThreadAndRunRequest;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIObjectRunThreadResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAICreateThreadAndRunRequestHandler extends AbstractRequestHandler<OpenAICreateThreadAndRunRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIObjectRunThreadResponse execute(final RequestContext ctx, final OpenAICreateThreadAndRunRequest request) throws Exception {
        final OpenAIThreadMessages messages = new OpenAIThreadMessages();
        messages.setMessages(request.getMessages());
        final OpenAIThreadRunReq runThreadReq = new OpenAIThreadRunReq();
        runThreadReq.setAssistantId(request.getAssistantId());
        runThreadReq.setThread(messages);
        final OpenAIObjectThreadRun initialState = openAIClient.createThreadAndRun(runThreadReq);
        final OpenAIObjectThreadRun result = openAIClient.loopUntilCompletion(ctx, initialState,
            T9tUtil.nvl(request.getMaxSeconds(), T9tOpenAIConstants.OPENAI_MAX_POLL_DURATION),
            T9tUtil.nvl(request.getPollMillis(), T9tOpenAIConstants.OPENAI_MAX_POLL_DURATION),
            null);
        final OpenAIObjectRunThreadResponse resp = new OpenAIObjectRunThreadResponse();
        resp.setResponse(result);
        return resp;
    }
}
