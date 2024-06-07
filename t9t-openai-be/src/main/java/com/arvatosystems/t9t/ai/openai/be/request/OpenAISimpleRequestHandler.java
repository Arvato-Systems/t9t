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
