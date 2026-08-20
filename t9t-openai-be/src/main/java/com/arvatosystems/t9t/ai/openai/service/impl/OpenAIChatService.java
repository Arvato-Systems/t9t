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
package com.arvatosystems.t9t.ai.openai.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaCategory;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.json.JsonParser;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiConversationDTO;
import com.arvatosystems.t9t.ai.AiResponseStructure;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectFile;
import com.arvatosystems.t9t.ai.openai.OpenAIPurposeType;
import com.arvatosystems.t9t.ai.openai.OpenAIResponseInputFormatType;
import com.arvatosystems.t9t.ai.openai.OpenAIRoleType;
import com.arvatosystems.t9t.ai.openai.OpenAITool;
import com.arvatosystems.t9t.ai.openai.responses.OpenAICreateResponseReq;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseInputContent;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseInputItem;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseInputJsonFormat;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseInputJsonSchema;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseOutputContent;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseOutputItem;
import com.arvatosystems.t9t.ai.openai.responses.OpenAIResponseResult;
import com.arvatosystems.t9t.ai.openai.responses.OpenAiReasoning;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.ai.service.IAiChatService;
import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;

@Singleton
@Named("OpenAI")
public class OpenAIChatService implements IAiChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIChatService.class);
    private static final int DEFAULT_MAX_TOOL_CALLS = 10;

    protected final IOpenAIClient openAIClient = Jdp.getRequired(IOpenAIClient.class);

    @Override
    public void validateMetadata(RequestContext ctx, Map<String, Object> metadata) {
        openAIClient.validateMetadata(metadata);
    }

    @Override
    public String createAssistant(final RequestContext ctx, final AiAssistantDTO assistantCfg) {
        // The Responses API does not use persistent assistant objects.
        // The model and instructions are sent with each request instead.
        // Return the t9t-side assistant ID as a no-op placeholder.
        LOGGER.info("createAssistant called for {} – Responses API does not create server-side assistant objects; skipping.",
          assistantCfg.getAiAssistantId());
        return assistantCfg.getAiAssistantId();
    }

    @Override
    public String startChat(final RequestContext ctx, final AiAssistantDTO assistantCfg) {
        // The Responses API does not use persistent threads.
        // Multi-turn conversation state is maintained via previousResponseId.
        // Return null; the first response ID will be stored after the first chat() call.
        return null;
    }

    private String cvtReasoningEnum(final Enum<?> reasoningEnum) {
        if (reasoningEnum == null) {
            return null;
        }
        return reasoningEnum.name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public MediaData chat(final RequestContext ctx, final AiAssistantDTO assistant, final AiConversationDTO conversation,
      final String question, final Object attachedDocumentRef, final MediaTypeDescriptor uploadedDocumentType,
      final List<String> textResponses, final boolean extractEmbeddedFileContent) {

        // Build the user input message
        final OpenAIResponseInputItem userMessage = new OpenAIResponseInputItem();
        userMessage.setType("message");
        userMessage.setRole(OpenAIRoleType.USER);
        userMessage.setContent(List.of(makeTextInput(question)));

        final List<OpenAIResponseInputItem> inputItems = new ArrayList<>(2);
        inputItems.add(userMessage);

        // Handle attached document (image or file reference as a note in the text)
        if (attachedDocumentRef != null) {
            if (uploadedDocumentType != null && uploadedDocumentType.getFormatCategory() == MediaCategory.IMAGE) {
                // Image attachments are included in the content via a dedicated message;
                // for simplicity, include the file reference as a text note.
                LOGGER.debug("Attached image file reference: {}", attachedDocumentRef);
            } else {
                // Non-image file; log for awareness – further integration may be added later.
                LOGGER.debug("Attached document reference: {}", attachedDocumentRef);
            }
        }

        // Build the Responses API request
        final OpenAICreateResponseReq req = createPartialResponseRequest(assistant, conversation.getProviderThreadId());

        req.setInput(inputItems);

        final int maxToolCalls = assistant.getToolsPermitted() ? JsonUtil.getZInteger(assistant.getZ(), "maxToolCalls", DEFAULT_MAX_TOOL_CALLS) : 0;

        // Execute the request (including any tool-call loops)
        LOGGER.debug("Calling Responses API (model={}, previousResponseId={}, maxToolCalls={})",
          req.getModel(), req.getPreviousResponseId(), maxToolCalls);

        final OpenAIResponseResult response = openAIClient.performOpenAICreateResponse(
          ctx, req, maxToolCalls, conversation.getObjectRef());

        // Persist the new response ID as the conversation thread ID for the next turn
        conversation.setProviderThreadId(response.getId());

        // Extract text from the response output
        final List<String> answers = new ArrayList<>();
        if (response.getOutput() != null) {
            for (final OpenAIResponseOutputItem item : response.getOutput()) {
                if ("message".equals(item.getType()) && item.getContent() != null) {
                    for (final OpenAIResponseOutputContent content : item.getContent()) {
                        if ("output_text".equals(content.getType()) && content.getText() != null) {
                            answers.add(content.getText());
                        }
                    }
                }
            }
        }

        if (answers.isEmpty()) {
            textResponses.add("(no answer)");
            return null;
        } else {
            final String answer = answers.get(0);

            if (extractEmbeddedFileContent) {
                // attempt to extract embedded file content (```<extension>\n<body>\n```)
                final int pos = answer.indexOf("```");
                if (pos >= 0) {
                    final int pos2 = answer.indexOf("```", pos + 3);
                    if (pos2 > 0) {
                        textResponses.add(answer.substring(0, pos) + answer.substring(pos2 + 3));
                        return extractMediaFromString(answer, pos + 3, pos2);
                    }
                }
                textResponses.add(answer);
                return null;
            }

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

    protected OpenAICreateResponseReq createPartialResponseRequest(final AiAssistantDTO assistant, final String previousResponseId) {
        final OpenAICreateResponseReq req = new OpenAICreateResponseReq();
        req.setModel(assistant.getModel());
        req.setInstructions(assistant.getInstructions());
        req.setPreviousResponseId(previousResponseId);
        req.setTemperature(assistant.getTemperature());
        req.setTopP(assistant.getTopP());
        req.setStore(assistant.getStore());
        if (assistant.getReasoningContext() != null || assistant.getReasoningEffort() != null || assistant.getReasoningMode() != null || assistant.getReasoningSummary() != null) {
            final OpenAiReasoning reasoning = new OpenAiReasoning();
            reasoning.setContext(cvtReasoningEnum(assistant.getReasoningContext()));
            reasoning.setEffort(cvtReasoningEnum(assistant.getReasoningEffort()));
            reasoning.setMode(cvtReasoningEnum(assistant.getReasoningMode()));
            reasoning.setSummary(cvtReasoningEnum(assistant.getReasoningSummary()));
            req.setReasoning(reasoning);
        }

        // Attach available tools if the assistant is permitted to use them
        final List<OpenAITool> tools;
        if (assistant.getToolsPermitted()) {
            tools = openAIClient.buildToolsFromStack(
              null, assistant.getExecutePermitted(), assistant.getDocumentAccessPermitted());
        } else {
            tools = List.of();
        }
        req.setTools(tools);

        return req;
    }

    protected OpenAIResponseInputContent makeTextInput(final String text) {
        final OpenAIResponseInputContent content = new OpenAIResponseInputContent();
        content.setType(OpenAIResponseInputFormatType.TEXT.getToken());
        content.setText(T9tUtil.nvl(text, ""));
        return content;
    }

    @Override
    public <T extends BonaPortable> AiResponseStructure<T> chat2(final RequestContext ctx, final AiAssistantDTO assistant,
            final String previousResponseId, final String dtoSpecificInstructions, final BonaPortable previousDto,
            final String userInput, final BonaPortable jsonOutputSchema, final String verbosity) {

        final List<OpenAIResponseInputItem> inputItems = new ArrayList<>(3);

        // Build the user input message
        final OpenAIResponseInputItem userMessage = new OpenAIResponseInputItem();
        final List<OpenAIResponseInputContent> contentList = new ArrayList<>(4);
        userMessage.setType("message");
        userMessage.setRole(OpenAIRoleType.USER);
        userMessage.setContent(contentList);

        if (dtoSpecificInstructions != null) {
            contentList.add(makeTextInput(dtoSpecificInstructions));
        }
        contentList.add(makeTextInput(userInput));
        inputItems.add(userMessage);

        if (previousDto != null) {
            // Add the previous DTO as a JSON input item
            final OpenAIResponseInputItem dtoMessage = new OpenAIResponseInputItem();
            dtoMessage.setType(OpenAIResponseInputFormatType.JSON.getToken());
            // dtoMessage.setRole(OpenAIRoleType.USER);
            dtoMessage.setValue(previousDto);
            inputItems.add(dtoMessage);
        }

        // Build the Responses API request
        final OpenAICreateResponseReq req = createPartialResponseRequest(assistant, previousResponseId);

        req.setInput(inputItems);

        if (jsonOutputSchema != null) {
            final OpenAIResponseInputJsonSchema inputJsonSchema = new OpenAIResponseInputJsonSchema();
            inputJsonSchema.setType("json_schema");
            inputJsonSchema.setName("InternalResponseWrapperDTO");
            // Info about a special requirement from OpenAI:
            // If strict=true all fields of the object must usually be in the schema's "required" list!
            // Optional parameters can be set with strict=true like this: "type": ["string", "null"]
            // This is a guideline for the schema generation in T9T. NOTE: strict=true is NOT tested yet!
            inputJsonSchema.setStrict(true);
            inputJsonSchema.setSchema(jsonOutputSchema);

            final OpenAIResponseInputJsonFormat inputJsonFormat = new OpenAIResponseInputJsonFormat();
            inputJsonFormat.setFormat(inputJsonSchema);
            inputJsonFormat.setVerbosity(verbosity);
            req.setText(inputJsonFormat);
        }

        final int maxToolCalls = assistant.getToolsPermitted() ? JsonUtil.getZInteger(assistant.getZ(), "maxToolCalls", DEFAULT_MAX_TOOL_CALLS) : 0;
        // Execute the request (including any tool-call loops)
        LOGGER.debug("Calling Responses API (model={}, previousResponseId={}, maxToolCalls={})",
          req.getModel(), req.getPreviousResponseId(), maxToolCalls);

        final OpenAIResponseResult response = openAIClient.performOpenAICreateResponse(ctx, req, maxToolCalls, null);
        final AiResponseStructure<T> result = new AiResponseStructure<>();
        result.setConversationId(response.getId());
        LOGGER.debug("Returned parameter from Responses API: conversationId = {}", response.getId());

        T resultObject = null;

        if (response.getOutput() != null) {
            for (final OpenAIResponseOutputItem item : response.getOutput()) {
                if (!"message".equals(item.getType()) || item.getContent() == null) {
                    continue;
                }
                for (final OpenAIResponseOutputContent content : item.getContent()) {
                    if (!"output_text".equals(content.getType()) || content.getText() == null) {
                        continue;
                    }
                    final Map<String, Object> parsedResponse = new JsonParser(content.getText(), false).parseObject();
                    final Object pqon = parsedResponse.get(MimeTypes.JSON_FIELD_PQON);
                    if (!(pqon instanceof String pqonString)) {
                        final String errorMessage = "Structured OpenAI response does not contain a valid @PQON field: " + parsedResponse.toString();
                        result.setErrorMessage(errorMessage);
                        LOGGER.error(errorMessage);
                        return result;
                    }
                    final BonaPortableClass<?> resultClass = BonaPortableFactory.getBClassForPqon(pqonString);
                    if (resultClass == null) {
                        final String errorMessage = "Unknown @PQON in structured OpenAI response: " + pqonString;
                        result.setErrorMessage(errorMessage);
                        LOGGER.error(errorMessage);
                        return result;
                    }
                    final BonaPortable parsedObject = resultClass.newInstance();
                    try {
                        parsedObject.deserialize(new MapParser(parsedResponse));
                        @SuppressWarnings("unchecked")
                        final T typedObject = (T) parsedObject;
                        resultObject = typedObject;
                        break;
                    } catch (final Exception e) {
                        final String errorMessage = "Can't deserialize the response DTO from OpenAI: " + e.getMessage();
                        result.setErrorMessage(errorMessage);
                        LOGGER.error("Can't deserialize the response DTO from OpenAI with error: {}, response={}", e.getMessage(), parsedResponse);
                        return result;
                    }
                }
                if (resultObject != null) {
                    break;
                }
            }
        }

        result.setResult(resultObject);
        return result;
    }
}
