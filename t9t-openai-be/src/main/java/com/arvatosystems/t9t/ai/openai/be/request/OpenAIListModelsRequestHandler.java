package com.arvatosystems.t9t.ai.openai.be.request;

import java.util.List;

import com.arvatosystems.t9t.ai.openai.request.AIModel;
import com.arvatosystems.t9t.ai.openai.request.OpenAIListModelsRequest;
import com.arvatosystems.t9t.ai.openai.request.OpenAIListModelsResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAIListModelsRequestHandler extends AbstractRequestHandler<OpenAIListModelsRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAIListModelsResponse execute(final RequestContext ctx, final OpenAIListModelsRequest request) throws Exception {
        final OpenAIListModelsResponse resp = new OpenAIListModelsResponse();
        final List<AIModel> models = openAIClient.getModels(request.getOnlyModel());
        if (Boolean.TRUE.equals(request.getSortByName())) {
            models.sort((m1, m2) -> m1.getModelId().compareTo(m2.getModelId()));
        }
        if (Boolean.TRUE.equals(request.getSortByDate())) {
            models.sort((m1, m2) -> m1.getWhenCreated().compareTo(m2.getWhenCreated()));
        }
        resp.setModels(models);
        return resp;
    }
}
