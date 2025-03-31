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
