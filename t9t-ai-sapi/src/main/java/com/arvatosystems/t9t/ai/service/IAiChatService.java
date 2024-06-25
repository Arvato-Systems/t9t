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
package com.arvatosystems.t9t.ai.service;

import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiConversationDTO;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Interface defining the interaction with a generic chat service.
 * Implementations should use a qualifier for their implementation,
 * for example OpenAI, Ollama, huggingface, kaggle etc.
 */
public interface IAiChatService {
    /** Validates generic metadata for specification limts. */
    void validateMetadata(@Nonnull RequestContext ctx, @Nullable Map<String, Object> metadata);

    /** Creates an assistant according to the specification. Returns the provider specific ID. */
    @Nonnull
    String createAssistant(@Nonnull RequestContext ctx, @Nonnull AiAssistantDTO assistantCfg);

    /** Starts a new chat thread. Returns a provider specific identifier of the thread. */
    String startChat(@Nonnull RequestContext ctx, @Nonnull AiAssistantDTO assistantCfg);

    /** Uploads a user document. returns a provider specific reference. */
    Object upload(@Nonnull RequestContext ctx, @Nonnull AiAssistantDTO assistant, @Nonnull AiConversationDTO conversation, @Nonnull MediaData document);

    /**
     * Sends a user request. Currently can return a String only.
     *
     * @param ctx          the request context
     * @param assistant    the assistant configuration
     * @param conversation the conversation data / status
     * @param question     the user input
     * @param attachedDocumentRef the provider specific reference to a previously thread related uploaded document, if any
     * @param uploadedDocumentType the type of the uploaded document, if any
     * @param textResponses a list of text responses to be filled by the chat service
     *
     * @return             a file output response from the chat service (optional)
     */
    @Nullable
    MediaData chat(@Nonnull RequestContext ctx, @Nonnull AiAssistantDTO assistant, @Nonnull AiConversationDTO conversation,
      @Nonnull String question, @Nullable Object attachedDocumentRef, @Nullable MediaTypeDescriptor uploadedDocumentType,
      @Nonnull List<String> textResponses);
}
