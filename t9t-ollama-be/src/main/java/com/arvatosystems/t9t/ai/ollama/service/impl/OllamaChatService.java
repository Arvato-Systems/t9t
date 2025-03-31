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
package com.arvatosystems.t9t.ai.ollama.service.impl;

import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiConversationDTO;
import com.arvatosystems.t9t.ai.service.IAiChatService;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("Ollama")
public class OllamaChatService implements IAiChatService {

    @Override
    public void validateMetadata(RequestContext ctx, Map<String, Object> metadata) {
    }

    @Override
    public String createAssistant(final RequestContext ctx, final AiAssistantDTO assistantCfg) {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED);
    }

    @Override
    public String startChat(final RequestContext ctx, final AiAssistantDTO assistantCfg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MediaData chat(final RequestContext ctx, final AiAssistantDTO assistant, final AiConversationDTO conversation,
      final String question, final Object attachedDocumentRef, final MediaTypeDescriptor uploadedDocumentType, final List<String> responses) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object upload(final RequestContext ctx, final AiAssistantDTO assistant, final AiConversationDTO conversation, final MediaData document) {
        // TODO Auto-generated method stub
        return null;
    }
}
