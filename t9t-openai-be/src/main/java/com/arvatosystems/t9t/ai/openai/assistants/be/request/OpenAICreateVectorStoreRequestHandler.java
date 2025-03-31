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
