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
package com.arvatosystems.t9t.ai.jpa.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiConversationDTO;
import com.arvatosystems.t9t.ai.T9tAIException;
import com.arvatosystems.t9t.ai.jpa.entities.AiAssistantEntity;
import com.arvatosystems.t9t.ai.jpa.entities.AiConversationEntity;
import com.arvatosystems.t9t.ai.jpa.entities.AiUserStatusEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiAssistantDTOMapper;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiAssistantDescriptionMapper;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiConversationDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiAssistantEntityResolver;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiConversationEntityResolver;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiUserStatusEntityResolver;
import com.arvatosystems.t9t.ai.request.AiChatRequest;
import com.arvatosystems.t9t.ai.request.AiChatResponse;
import com.arvatosystems.t9t.ai.service.IAIChatService;
import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;

public class AiChatRequestHandler extends AbstractRequestHandler<AiChatRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatRequestHandler.class);

    private final IAiAssistantDTOMapper assistantMapper = Jdp.getRequired(IAiAssistantDTOMapper.class);
    private final IAiAssistantDescriptionMapper assistantDescMapper = Jdp.getRequired(IAiAssistantDescriptionMapper.class);
    private final IAiAssistantEntityResolver assistantResolver = Jdp.getRequired(IAiAssistantEntityResolver.class);
    private final IAiUserStatusEntityResolver userStatusResolver = Jdp.getRequired(IAiUserStatusEntityResolver.class);
    private final IAiConversationEntityResolver conversationResolver = Jdp.getRequired(IAiConversationEntityResolver.class);
    private final IAiConversationDTOMapper conversationMapper = Jdp.getRequired(IAiConversationDTOMapper.class);

    @Override
    public AiChatResponse execute(final RequestContext ctx, final AiChatRequest request) throws Exception {
        final AiChatResponse response = new AiChatResponse();
        final String userInput = request.getUserInput();
        final MediaData userUpload = request.getUserUpload();
        final AiConversationEntity conversation = getConversation(ctx);

        if (conversation == null) {
            // no assistant available
            LOGGER.error("No assistant has been configured");
            throw new T9tException(T9tAIException.NO_ASSISTANT);
        }
        final AiAssistantEntity assistant = conversation.getAiAssistant();
        response.setAiAssistant(assistantDescMapper.mapToDto(assistant));

        if (userInput == null && userUpload == null) {
            // all input is null: initial prompt request
            response.setTextOutput(List.of(assistant.getGreeting()));
        } else {
            final AiConversationDTO conversationDto = conversationMapper.mapToDto(conversation);
            final AiAssistantDTO assistantDto = assistantMapper.mapToDto(assistant);
            final IAIChatService chatService = Jdp.getRequired(IAIChatService.class, assistantDto.getAiProvider());
            final List<String> responses = new ArrayList<>();
            Object uploadedDocumentRef = null;
            MediaTypeDescriptor mtd = null;

            if (userUpload != null) {
                // upload a file

                final Map<String, Object> fileRefs = conversationDto.getFileReferences() != null ? conversationDto.getFileReferences() : new HashMap<>();
                mtd = MediaTypeInfo.getFormatByType(userUpload.getMediaType());
                String filename = JsonUtil.getZString(userUpload.getZ(), T9tConstants.MEDIA_DATA_Z_KEY_FILENAME, null);
                if (filename == null) {
                    // invent one and store that
                    filename = "F" + conversation.getNumberOfFilesAdded();
                    userUpload.setZ(Map.of(T9tConstants.MEDIA_DATA_Z_KEY_FILENAME, filename));
                }
                uploadedDocumentRef = chatService.upload(ctx, assistantDto, conversationDto, userUpload);
                LOGGER.debug("Uploaded document {} of type {}: result is reference {}", filename, userUpload.getMediaType(), uploadedDocumentRef);
                if (uploadedDocumentRef != null) {
                    fileRefs.put(filename, uploadedDocumentRef);
                    // also update the entity
                    conversation.setFileReferences(fileRefs);
                    conversation.setNumberOfFilesAdded(conversation.getNumberOfFilesAdded() + 1);
                    responses.add("File " + filename + " uploaded as " + uploadedDocumentRef.toString());
                }
            }
            if (userInput != null) {
                response.setMediaOutput(chatService.chat(ctx, assistantDto, conversationDto, userInput, uploadedDocumentRef, mtd, responses));
                conversation.setNumberOfMessages(conversation.getNumberOfMessages() + 1);
            }
            response.setTextOutput(responses);
        }
        return response;
    }

    // get or create a conversation entity. Return null if no assistant has been configured.
    private AiConversationEntity getConversation(final RequestContext ctx) {
        final Long sessionRef = ctx.internalHeaderParameters.getJwtInfo().getSessionRef();

        // get or create a current user status
        final List<AiUserStatusEntity> statusList = userStatusResolver.findByUserId(false, ctx.userId);
        final AiUserStatusEntity userStatus;
        if (!statusList.isEmpty()) {
            userStatus = statusList.get(0);
        } else {
            // create a new status
            userStatus = userStatusResolver.newEntityInstance();
            userStatus.setObjectRef(userStatusResolver.createNewPrimaryKey());
            userStatus.setUserId(ctx.userId);
            userStatusResolver.save(userStatus);  // temp save without preferred assistant
        }
        // get the conversation, if one exists
        final List<AiConversationEntity> conversations = conversationResolver.findByUserIdAndSessionRef(false, ctx.userId, sessionRef);
        if (!conversations.isEmpty()) {
            return conversations.get(0);
        }
//        if (userStatus.getCurrentConversation() != null && sessionRef.equals(userStatus.getCurrentConversation().getCreatedBySessionRef())) {
//            // conversation exists
//            return userStatus.getCurrentConversation();
//        }
        final AiAssistantEntity assistant;
        if (userStatus.getPreferredAssistant() != null) {
            // no assistant configured - cannot set up
            assistant = userStatus.getPreferredAssistant();
        } else {
            // try to find one
            final List<AiAssistantEntity> allAssistants = assistantResolver.findByAllAssistants(true);
            if (allAssistants.isEmpty()) {
                // no assistant available
                return null;
            }
            assistant = allAssistants.get(0);
            // also set it as the preferred one
            userStatus.setPreferredAssistantRef(assistant.getObjectRef());
        }
        // create a new conversation
        return createNewConversation(ctx, assistant);
    }

    private AiConversationEntity createNewConversation(final RequestContext ctx, final AiAssistantEntity assistant) {
        final Long sessionRef = ctx.internalHeaderParameters.getJwtInfo().getSessionRef();
        final AiConversationEntity newConversation = conversationResolver.newEntityInstance();
        newConversation.setObjectRef(conversationResolver.createNewPrimaryKey());
        newConversation.setUserId(ctx.userId);
        newConversation.setCreatedBySessionRef(sessionRef);
        newConversation.setAiAssistantRef(assistant.getObjectRef());
        newConversation.setAiAssistant(assistant);
        // obtain a provider thread ID
        final IAIChatService chatService = Jdp.getRequired(IAIChatService.class, assistant.getAiProvider());
        final String threadId = chatService.startChat(ctx, assistantMapper.mapToDto(assistant));
        newConversation.setProviderThreadId(threadId);
        conversationResolver.save(newConversation);
        return newConversation;
    }
}
