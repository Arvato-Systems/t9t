package com.arvatosystems.t9t.ai.openai.be.request;

import com.arvatosystems.t9t.ai.openai.OpenAIObjectCreateEmbeddings;
import com.arvatosystems.t9t.ai.openai.request.OpenAICreateEmbeddingsRequest;
import com.arvatosystems.t9t.ai.openai.request.OpenAICreateEmbeddingsResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAICreateEmbeddingsRequestHandler extends AbstractRequestHandler<OpenAICreateEmbeddingsRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAICreateEmbeddingsResponse execute(final RequestContext ctx, final OpenAICreateEmbeddingsRequest request) throws Exception {
        final OpenAIObjectCreateEmbeddings response = openAIClient.performOpenAICreateEmbeddings(request.getRequest());
        final OpenAICreateEmbeddingsResponse resp = new OpenAICreateEmbeddingsResponse();
        resp.setResponse(response);
        return resp;
    }
}
