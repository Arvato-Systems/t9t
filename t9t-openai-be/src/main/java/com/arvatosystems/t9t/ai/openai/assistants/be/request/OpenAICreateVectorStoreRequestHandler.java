package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectVectorStore;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAICreateVectorStoreRequest;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIVectorStoreResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAICreateVectorStoreRequestHandler extends AbstractRequestHandler<OpenAICreateVectorStoreRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIVectorStoreResponse execute(final RequestContext ctx, final OpenAICreateVectorStoreRequest request) throws Exception {

        final OpenAIObjectVectorStore vectorStore = openAIClient.createVectorStore(request.getVectorStore());
        final OpenAIVectorStoreResponse resp = new OpenAIVectorStoreResponse();
        resp.setVectorStore(vectorStore);
        return resp;
    }
}
