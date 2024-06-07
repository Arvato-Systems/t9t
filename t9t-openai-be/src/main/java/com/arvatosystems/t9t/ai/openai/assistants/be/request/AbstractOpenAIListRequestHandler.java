package com.arvatosystems.t9t.ai.openai.assistants.be.request;

import com.arvatosystems.t9t.ai.openai.AbstractOpenAIListObject;
import com.arvatosystems.t9t.ai.openai.AbstractOpenAIObjectWithId;
import com.arvatosystems.t9t.ai.openai.assistants.request.AbstractOpenAIListRequest;
import com.arvatosystems.t9t.ai.openai.assistants.request.OpenAIListResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;

import de.jpaw.dp.Jdp;

public abstract class AbstractOpenAIListRequestHandler<T extends AbstractOpenAIObjectWithId, R extends AbstractOpenAIListRequest<T>>
  extends AbstractReadOnlyRequestHandler<R> {

    protected final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    protected OpenAIListResponse<T> createResult(final AbstractOpenAIListObject<T> data) {
        final OpenAIListResponse<T> resp = new OpenAIListResponse<>();
        resp.setData(data.getData());
        resp.setReturnCode(0);
        resp.setHasMore(data.getHasMore());
        resp.setFirstId(data.getFirstId());
        resp.setLastId(data.getLastId());
        return resp;
    }
}
