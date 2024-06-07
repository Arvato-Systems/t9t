package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.T9tOpenAIConstants;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadRun;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIRunThreadRequest;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIObjectRunThreadResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAIRunThreadRequestHandler extends AbstractRequestHandler<OpenAIRunThreadRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIObjectRunThreadResponse execute(final RequestContext ctx, final OpenAIRunThreadRequest request) throws Exception {
        final OpenAIObjectThreadRun res = openAIClient.createRunAndLoop(ctx, request.getThreadId(), request.getData(),
            T9tUtil.nvl(request.getMaxSeconds(), T9tOpenAIConstants.OPENAI_MAX_TIME),
            T9tUtil.nvl(request.getPollMillis(), T9tOpenAIConstants.OPENAI_MAX_POLL_DURATION));
        final OpenAIObjectRunThreadResponse resp = new OpenAIObjectRunThreadResponse();
        resp.setResponse(res);
        return resp;
    }
}
