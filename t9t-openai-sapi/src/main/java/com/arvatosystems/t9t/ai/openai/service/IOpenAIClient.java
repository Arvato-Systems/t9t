/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import com.arvatosystems.t9t.ai.openai.OpenAIChatCompletionReq;
import com.arvatosystems.t9t.ai.openai.OpenAICreateEmbeddingsReq;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectChatCompletion;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectCreateEmbeddings;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectFile;
import com.arvatosystems.t9t.ai.openai.OpenAIPurposeType;
import com.arvatosystems.t9t.ai.openai.OpenAIQueryParameters;
import com.arvatosystems.t9t.ai.openai.OpenAITool;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAICreateAssistantReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAICreateVectorStoreReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectAssistant;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectListAssistants;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectListThreadMessages;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectListThreadRuns;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadRun;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectVectorStore;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadRunReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThread;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadMessageReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIToolOutputReq;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Interface for the OpenAI client.
 * The implementation performs the low level calls to the OpenAI API.
 */
public interface IOpenAIClient {
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


    /** Creates an assistant. */
    @Nonnull OpenAIObjectAssistant createAssistant(@Nonnull OpenAICreateAssistantReq assistantReq,
      boolean addAllTools, boolean allowCoding, boolean allowFileSearch);

    /** Retrieves a specific assistant. */
    @Nonnull OpenAIObjectAssistant getAssistantById(@Nonnull String assistantId);

    /** List existing assistants. */
    @Nonnull OpenAIObjectListAssistants listAssistants(@Nullable OpenAIQueryParameters queryParameters);

    /** Creates a thread. */
    @Nonnull OpenAIThread createThread();

    /** Retrieves a specific thread. */
    @Nonnull OpenAIThread getThreadById(@Nonnull String threadId);

    /** Adds messages to a specific thread. */
    @Nonnull void addMessagesToThread(@Nonnull String threadId, @Nonnull List<OpenAIThreadMessageReq> messages);

    /** Starts a prepared thread. */
    @Nonnull OpenAIObjectThreadRun createRun(@Nonnull String threadId, @Nonnull OpenAIThreadRunReq request);

    /** Creates a new thread and starts it. */
    @Nonnull OpenAIObjectThreadRun createThreadAndRun(@Nonnull OpenAIThreadRunReq request);

    @Nonnull OpenAIObjectThreadRun loopUntilCompletion(@Nonnull RequestContext ctx, @Nonnull OpenAIObjectThreadRun initialState,
         int maxSeconds, long pollMillis, Long conversationRef);

    /** List existing runs for a given thread. */
    @Nonnull OpenAIObjectListThreadRuns listThreadRuns(@Nonnull String threadId, @Nullable OpenAIQueryParameters queryParameters);

    /** List existing messages for a given thread. */
    @Nonnull OpenAIObjectListThreadMessages listThreadMessages(@Nonnull String threadId, @Nullable OpenAIQueryParameters queryParameters);

    /** Retrieves a run status. */
    @Nonnull OpenAIObjectThreadRun getRun(@Nonnull String threadId, @Nonnull String runId);

    /** Submit tool output to a run. */
    @Nonnull OpenAIObjectThreadRun submitToolOutputs(@Nonnull String threadId, @Nonnull String runId, @Nonnull OpenAIToolOutputReq toolOutputs);

    /** Starts a prepared thread amd loops until output is available or a problem encountered, or the maximum time exceeded. */
    @Nonnull OpenAIObjectThreadRun createRunAndLoop(@Nonnull RequestContext ctx,  @Nonnull String threadId, @Nonnull OpenAIThreadRunReq request,
      int maxSeconds, long pollMillis, Long conversationRef);

    /** Creates an assistant. */
    @Nonnull OpenAIObjectVectorStore createVectorStore(@Nonnull OpenAICreateVectorStoreReq createVectorStoreReq);
}
