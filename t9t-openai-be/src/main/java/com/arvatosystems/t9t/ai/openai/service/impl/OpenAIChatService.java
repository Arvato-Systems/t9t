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
package com.arvatosystems.t9t.ai.openai.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiConversationDTO;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectFile;
import com.arvatosystems.t9t.ai.openai.OpenAIPurposeType;
import com.arvatosystems.t9t.ai.openai.OpenAIResponseFormatType;
import com.arvatosystems.t9t.ai.openai.OpenAIRoleType;
import com.arvatosystems.t9t.ai.openai.OpenAITool;
import com.arvatosystems.t9t.ai.openai.OpenAIToolType;
import com.arvatosystems.t9t.ai.openai.T9tOpenAIConstants;
import com.arvatosystems.t9t.ai.openai.assistants.AbstractOpenAIContent;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIAttachment;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAICreateAssistantReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIImageFile;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectAssistant;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectListThreadMessages;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadMessage;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadRun;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAISubImageFile;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThread;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadMessageReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadOut;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadRunReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadText;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.ai.service.IAiChatService;
import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaCategory;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("OpenAI")
public class OpenAIChatService implements IAiChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIChatService.class);

    protected final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public void validateMetadata(RequestContext ctx, Map<String, Object> metadata) {
        openAIClient.validateMetadata(metadata);
    }

    /** Builds an OpenAI request object from the generic assistant configuration. */
    protected OpenAICreateAssistantReq buildCreateAssistantRequest(final AiAssistantDTO assistantCfg) {
        final OpenAICreateAssistantReq req = new OpenAICreateAssistantReq();
        req.setModel(assistantCfg.getModel());
        req.setName(assistantCfg.getAiAssistantId());
        req.setDescription(assistantCfg.getDescription());
        req.setTemperature(assistantCfg.getTemperature());
        // req.setMaxTokens(assistantCfg.getMaxTokens());
        req.setTopP(assistantCfg.getTopP());
        req.setMetadata(assistantCfg.getMetadata());
        return req;
    }

    @Override
    public String createAssistant(final RequestContext ctx, final AiAssistantDTO assistantCfg) {
        // create the OpenAI related object for assistant creation
        final OpenAICreateAssistantReq req = buildCreateAssistantRequest(assistantCfg);

        // call the OpenAI client
        final OpenAIObjectAssistant asst = openAIClient.createAssistant(req,
                assistantCfg.getToolsPermitted(), assistantCfg.getExecutePermitted(), assistantCfg.getDocumentAccessPermitted());
        return asst.getId();
    }

    @Override
    public String startChat(final RequestContext ctx, final AiAssistantDTO assistantCfg) {
        // convert internal config into OpenAI request
        final OpenAIThread thread = openAIClient.createThread();
        return thread.getId();
    }

    @Override
    public MediaData chat(final RequestContext ctx, final AiAssistantDTO assistant, final AiConversationDTO conversation,
      final String question, final Object attachedDocumentRef, final MediaTypeDescriptor uploadedDocumentType, final List<String> textResponses) {
        // add the message
        final List<AbstractOpenAIContent> contentList = new ArrayList<>(4);
        final OpenAIThreadMessageReq message = new OpenAIThreadMessageReq(OpenAIRoleType.USER, contentList);
        message.setContent(contentList);
        if (attachedDocumentRef != null) {
            if (uploadedDocumentType != null && uploadedDocumentType.getFormatCategory() == MediaCategory.IMAGE) {
                // add an image
                final OpenAISubImageFile subImageFile = new OpenAISubImageFile(attachedDocumentRef.toString());
                final OpenAIImageFile imageFile = new OpenAIImageFile(OpenAIResponseFormatType.IMAGE_FILE, subImageFile);
                contentList.add(imageFile);
            } else {
                // add a text document
                final OpenAIAttachment attachedDocument = new OpenAIAttachment();
                attachedDocument.setFileId(attachedDocumentRef.toString());
                attachedDocument.setTools(List.of(new OpenAITool(OpenAIToolType.FILE_SEARCH)));
                message.setAttachments(List.of(attachedDocument));
            }
        }
        if (question != null) {
            // final OpenAISubThreadText subText = new OpenAISubThreadText(question);
            final OpenAIThreadText threadText = new OpenAIThreadText(OpenAIResponseFormatType.TEXT, question);
            contentList.add(threadText);
        }
        openAIClient.addMessagesToThread(conversation.getProviderThreadId(), List.of(message));

        // perform the evaluation loop
        final int maxTime = JsonUtil.getZInteger(assistant.getZ(), "maxTime", T9tOpenAIConstants.OPENAI_MAX_TIME);
        final int maxPollDuration = JsonUtil.getZInteger(assistant.getZ(), "maxPollDuration", T9tOpenAIConstants.OPENAI_MAX_POLL_DURATION);
        final OpenAIThreadRunReq runObj = new OpenAIThreadRunReq();
        runObj.setAssistantId(assistant.getAiAssistantId());
        final OpenAIObjectThreadRun res = openAIClient.createRunAndLoop(ctx, conversation.getProviderThreadId(), runObj, maxTime, maxPollDuration,
          conversation.getObjectRef());

        // obtain results
        final OpenAIObjectListThreadMessages msgs = openAIClient.listThreadMessages(conversation.getProviderThreadId(), null);
        final List<String> answers = new ArrayList<>();
        for (final OpenAIObjectThreadMessage msg : msgs.getData()) {
            LOGGER.debug("Received MSG RESULT role {}, {} messages, attachments = {}", msg.getRole(), msg.getContent().size(), msg.getAttachments().size());
            if (!msg.getContent().isEmpty()) {
                for (final OpenAIThreadOut out : msg.getContent()) {
                    if (out.getText() != null) {
                        // standard text output
                        answers.add(out.getText().getValue());
                    } else {
                        // some image reference or other type of output
                        LOGGER.debug("Received MSG RESULT type = {}, full dat ais {}", out.getType(), ToStringHelper.toStringML(out));
                    }
                }
            }
        }
        if (answers.isEmpty()) {
            textResponses.add("(no answer)");
            return null;
        } else {
            // attempt to extract some embedded file
            final String answer = answers.get(0);
            // attempt to extract a file
            final int pos = answer.indexOf("```");
            if (pos >= 0) {
                final int pos2 = answer.indexOf("```", pos + 3);
                if (pos2 > 0) {
                    textResponses.add(answer.substring(0, pos) + answer.substring(pos2 + 3));
                    return extractMediaFromString(answer, pos + 3, pos2);
                }
            }
            // only a text response
            textResponses.add(answer);
            return null;
        }
    }

    private static final MediaTypeDescriptor RAW_INFO = MediaTypeInfo.getFormatByMimeType("text/plain");

    private MediaData extractMediaFromString(final String data, final int startExtension, final int endData) {
        final StringBuilder extension = new StringBuilder(8);
        int s = startExtension;
        while (s < endData && data.charAt(s) != '\n' && data.charAt(s) != '\r') {
            extension.append(data.charAt(s));
            ++s;
        }
        while (s < endData && (data.charAt(s) == '\n' || data.charAt(s) == '\r')) {
            ++s;
        }
        final String fileExtension = extension.toString();
        final MediaTypeDescriptor mtd = T9tUtil.nvl(MediaTypeInfo.getFormatByFileExtension(fileExtension), RAW_INFO);
        LOGGER.debug("Extracted file extension {} from data, media type is {}", fileExtension, mtd.getMediaType());
        final MediaData md = new MediaData(mtd.getMediaType());
        md.setText(data.substring(s, endData));
        return md;
    }

    @Override
    public String upload(final RequestContext ctx, final AiAssistantDTO assistant, final AiConversationDTO conversation, final MediaData document) {
        final MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByType(document.getMediaType());
        final OpenAIPurposeType purpose = mtd != null && mtd.getFormatCategory() == MediaCategory.IMAGE
          ? OpenAIPurposeType.VISION : OpenAIPurposeType.ASSISTANTS;
        final OpenAIObjectFile fileData = openAIClient.performOpenAIFileUpload(document, purpose);
        return fileData.getId();
    }
}
