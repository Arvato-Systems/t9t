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
package com.arvatosystems.t9t.ai.openai.service;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import de.jpaw.bonaparte.pojos.api.media.MediaData;

import com.arvatosystems.t9t.ai.openai.OpenAIChatCompletionReq;
import com.arvatosystems.t9t.ai.openai.OpenAICreateEmbeddingsReq;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectChatCompletion;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectCreateEmbeddings;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectFile;
import com.arvatosystems.t9t.ai.openai.OpenAIPurposeType;
import com.arvatosystems.t9t.ai.openai.OpenAIQueryParameters;
import com.arvatosystems.t9t.ai.openai.OpenAITool;
import com.arvatosystems.t9t.ai.openai.request.AIModel;
import com.arvatosystems.t9t.ai.openai.responses.OpenAICreateResponseReq;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseResult;
import com.arvatosystems.t9t.base.services.RequestContext;

/**
 * Interface for the OpenAI client.
 * The implementation performs the low level calls to the OpenAI API.
 */
public interface IOpenAIClient {
    /** Returns the lit of available models. */
    @Nonnull List<AIModel> getModels(@Nullable String onlyModel);

    /** Validates metadata. */
    void validateMetadata(@Nullable Map<String, Object> metadata);

    /** Creates query parameters from specification, and appends them to the path. */
    String addQueryParameters(@Nonnull String path, @Nullable OpenAIQueryParameters queryParameters);

    /** Performs a chat completion. */
    @Nonnull OpenAIObjectChatCompletion performOpenAIChatCompletion(@Nonnull OpenAIChatCompletionReq request);

    /** Performs a chat completion, performs tool calls. */
    @Nonnull OpenAIObjectChatCompletion performOpenAIChatCompletionWithToolCalls(@Nonnull RequestContext ctx, @Nonnull OpenAIChatCompletionReq request,
      @Nullable List<String> toolSelection, int maxToolCalls);

    /** Helper to build the tools parameter from a tool stack list. */
    @Nonnull List<OpenAITool> buildToolsFromStack(@Nullable List<String> selection, boolean allowCoding, boolean allowFileSearch);

    /** Computes embeddings. */
    @Nonnull OpenAIObjectCreateEmbeddings performOpenAICreateEmbeddings(@Nonnull OpenAICreateEmbeddingsReq request);


    /** Uploads a file. */
    @Nonnull OpenAIObjectFile performOpenAIFileUpload(@Nonnull MediaData content, @Nonnull OpenAIPurposeType purpose);

    /**
     * Creates a response using the Responses API (/v1/responses), performing tool calls as needed.
     * This replaces the deprecated assistants thread/run workflow.
     *
     * @param ctx             the RequestContext
     * @param request         the response request (model, input, instructions, previousResponseId, tools)
     * @param maxToolCalls    maximum number of tool calls to execute (0 = no tools)
     * @param conversationRef optional reference for logging tool calls
     * @return the final response result
     */
    @Nonnull OpenAIResponseResult performOpenAICreateResponse(@Nonnull RequestContext ctx, @Nonnull OpenAICreateResponseReq request,
      int maxToolCalls, @Nullable Long conversationRef);
}
