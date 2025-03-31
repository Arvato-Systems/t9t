/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
