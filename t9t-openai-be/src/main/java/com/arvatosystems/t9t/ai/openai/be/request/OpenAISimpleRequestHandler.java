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

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.ai.openai.OpenAIChatCompletionChoice;
import com.arvatosystems.t9t.ai.openai.OpenAIChatCompletionReq;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectChatCompletion;
import com.arvatosystems.t9t.ai.openai.OpenAIMessage;
import com.arvatosystems.t9t.ai.openai.OpenAIRoleType;
import com.arvatosystems.t9t.ai.openai.request.OpenAISimpleRequest;
import com.arvatosystems.t9t.ai.openai.request.OpenAISimpleResponse;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class OpenAISimpleRequestHandler extends AbstractRequestHandler<OpenAISimpleRequest> {

    private final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public OpenAISimpleResponse execute(final RequestContext ctx, final OpenAISimpleRequest request) throws Exception {
        final OpenAIChatCompletionReq chatRq = new OpenAIChatCompletionReq();

        final List<OpenAIMessage> messages = new ArrayList<>();  // must be mutable for now
        chatRq.setModel("gpt-3.5-turbo");
        chatRq.setMessages(messages);
        final OpenAIMessage systemMessage = new OpenAIMessage();
        systemMessage.setRole(OpenAIRoleType.SYSTEM);
        systemMessage.setContent("You are a helpful assistant.");
        messages.add(systemMessage);
        final OpenAIMessage userMessage = new OpenAIMessage();
        userMessage.setRole(OpenAIRoleType.USER);
        userMessage.setContent(request.getQuestion());
        chatRq.setTemperature(0.0f);

        final OpenAIObjectChatCompletion response = openAIClient.performOpenAIChatCompletionWithToolCalls(ctx, chatRq, null, 10);
        final OpenAIChatCompletionChoice choice = response.getChoices().get(0);
        final String answer = choice.getMessage().getContent();
        final OpenAISimpleResponse resp = new OpenAISimpleResponse();
        resp.setAnswer(answer);
        return resp;
    }
}
