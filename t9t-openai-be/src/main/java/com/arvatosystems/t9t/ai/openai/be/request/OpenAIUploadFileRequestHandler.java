package com.arvatosystems.t9t.ai.openai.be.request;

import com.arvatosystems.t9t.ai.openai.request.OpenAIUploadFileRequest;
import com.arvatosystems.t9t.ai.openai.request.OpenAIUploadFileResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAIUploadFileRequestHandler extends AbstractRequestHandler<OpenAIUploadFileRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIUploadFileResponse execute(final RequestContext ctx, final OpenAIUploadFileRequest request) throws Exception {
        final OpenAIUploadFileResponse resp = new OpenAIUploadFileResponse();
        resp.setFile(openAIClient.performOpenAIFileUpload(request.getContent(), request.getPurpose()));
        return resp;
    }
}
