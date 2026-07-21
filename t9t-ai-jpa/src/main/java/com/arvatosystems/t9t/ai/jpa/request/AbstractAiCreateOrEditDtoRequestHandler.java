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
package com.arvatosystems.t9t.ai.jpa.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.dp.Jdp;
import de.jpaw.json.JsonParser;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiConversationDTO;
import com.arvatosystems.t9t.ai.AiDtoAssistKey;
import com.arvatosystems.t9t.ai.T9tAiException;
import com.arvatosystems.t9t.ai.T9tAiTools;
import com.arvatosystems.t9t.ai.jpa.entities.AiDtoAssistEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiAssistantDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiAssistantEntityResolver;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiDtoAssistEntityResolver;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaObject;
import com.arvatosystems.t9t.ai.request.AbstractAiCreateOrEditDtoRequest;
import com.arvatosystems.t9t.ai.request.AiCreateOrEditDtoResponse;
import com.arvatosystems.t9t.ai.request.ChatStatus;
import com.arvatosystems.t9t.ai.service.IAiChatService;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public abstract class AbstractAiCreateOrEditDtoRequestHandler<T extends BonaPortable, R extends AbstractAiCreateOrEditDtoRequest<T>> extends AbstractRequestHandler<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAiCreateOrEditDtoRequestHandler.class);

    private static final String PLACEHOLDER_ROLE = "##ROLE##";
    private static final String PLACEHOLDER_JSON_SCHEMA = "##JSON_SCHEMA##";
    private static final String PLACEHOLDER_EXISTING_JSON = "##EXISTING_JSON##";
    private static final String PLACEHOLDER_USER_PROMPT = "##USER_PROMPT##";

    protected final IAiDtoAssistEntityResolver dtoAssistResolver = Jdp.getRequired(IAiDtoAssistEntityResolver.class);
    protected final IAiAssistantEntityResolver assistantResolver = Jdp.getRequired(IAiAssistantEntityResolver.class);
    protected final IAiAssistantDTOMapper assistantMapper = Jdp.getRequired(IAiAssistantDTOMapper.class);

    protected AiCreateOrEditDtoResponse<T> execute(final RequestContext ctx, final AbstractAiCreateOrEditDtoRequest<T> request, final BonaPortableClass<T> bclass) throws Exception {
        // read the configuration for the given DTO
        final AiDtoAssistEntity dtoAssist = dtoAssistResolver.getEntityData(new AiDtoAssistKey(bclass.getPqon()));

        // get the assistant configuration
        final AiAssistantDTO assistant = assistantMapper.mapToDto(dtoAssist.getAiAssistant());

        // get or generate the JSON schema
        final String jsonSchema;
        if (!T9tUtil.isBlank(dtoAssist.getDtoSchema())) {
            jsonSchema = dtoAssist.getDtoSchema();
        } else {
            // auto-generate JSON schema from class definition
            final JsonSchemaObject schemaObj = T9tAiTools.buildJsonSchemaObject(bclass.getMetaData(), null);
            jsonSchema = JsonComposer.toJsonString(schemaObj);
        }

        // create a conversation DTO for this interaction
        final AiConversationDTO conversation = new AiConversationDTO();
        conversation.setProviderThreadId(request.getConversationId());

        // get the instruction string and replace all subtexts like ROLE, DTO_SCHEMA and USER_PROMPT
        final String baseInstructions = dtoAssist.getDtoInstructions();
        final String fullPrompt = baseInstructions
                .replace(PLACEHOLDER_ROLE, dtoAssist.getRole())
                .replace(PLACEHOLDER_JSON_SCHEMA, jsonSchema)
                .replace(PLACEHOLDER_EXISTING_JSON, request.getDto() != null ? JsonComposer.toJsonString(request.getDto()) : "")
                .replace(PLACEHOLDER_USER_PROMPT, request.getChatInput());

        // call the LLM service
        final IAiChatService chatService = Jdp.getRequired(IAiChatService.class, assistant.getAiProvider());
        final List<String> textResponses = new ArrayList<>();
        chatService.chat(ctx, assistant, conversation, fullPrompt, null, null, textResponses, false);

        // build response
        final AiCreateOrEditDtoResponse<T> response = new AiCreateOrEditDtoResponse<>();
        response.setConversationId(conversation.getProviderThreadId());

        // try to parse the LLM response as JSON
        if (!textResponses.isEmpty()) {
            final String llmResponse = textResponses.get(0);
            response.setResponse(llmResponse);

            try {
                // try to extract JSON from response (LLM might wrap it in markdown code blocks)
                final String jsonContent = extractJson(llmResponse);

                if (jsonContent != null) {
                    // parse JSON to Map
                    final JsonParser jp = new JsonParser(jsonContent, false);
                    final Map<String, Object> map = jp.parseObject();

                    // add PQON to map so MapParser can detect the correct type
                    map.put(MimeTypes.JSON_FIELD_PQON, bclass.getPqon());

                    // convert map to BonaPortable
                    final T dto = (T) MapParser.asBonaPortable(map, MapParser.OUTER_BONAPORTABLE_FOR_JSON);
                    response.setDto(dto);
                    response.setChatStatus(ChatStatus.DONE);

                    LOGGER.info("Successfully created/edited DTO of type {} via AI assistant", bclass.getPqon());
                } else {
                    // no JSON found, LLM is asking for more information
                    response.setChatStatus(ChatStatus.NEED_MORE_INFORMATION);
                    LOGGER.debug("LLM response does not contain valid JSON content, needs more information");
                }
            } catch (final Exception e) {
                // failed to parse - LLM might be asking for clarification
                LOGGER.error("Failed to parse LLM response as JSON for DTO {}: {}", bclass.getPqon(), e.getMessage());
                response.setChatStatus(ChatStatus.NEED_MORE_INFORMATION);
            }
        } else {
            // no response from LLM
            response.setChatStatus(ChatStatus.NEED_MORE_INFORMATION);
            response.setResponse("No response from AI assistant");
        }

        return response;
    }

    /**
     * Extracts JSON content from LLM response, handling markdown code blocks.
     */
    private String extractJson(final String response) {
        if (response == null) {
            return null;
        }

        // try to find JSON in markdown code blocks
        final String[] codeBlockMarkers = {"```json", "```"};
        for (final String marker : codeBlockMarkers) {
            final int startIndex = response.indexOf(marker);
            if (startIndex != -1) {
                final int jsonStart = response.indexOf('\n', startIndex + marker.length());
                if (jsonStart != -1) {
                    final int jsonEnd = response.indexOf("```", jsonStart);
                    if (jsonEnd != -1) {
                        return response.substring(jsonStart + 1, jsonEnd).trim();
                    }
                }
            }
        }

        // if no code blocks, try to find JSON by looking for { }
        final int startBrace = response.indexOf('{');
        final int endBrace = response.lastIndexOf('}');
        if (startBrace != -1 && endBrace != -1 && endBrace > startBrace) {
            return response.substring(startBrace, endBrace + 1);
        }

        // check if entire response is JSON
        final String trimmed = response.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }

        return null;
    }

    protected AiCreateOrEditDtoResponse<T> execute(final RequestContext ctx, final AbstractAiCreateOrEditDtoRequest<T> request, final String pqon) throws Exception {
        // determine the meta-class for the PQON
        try {
            final BonaPortableClass<T> bclass = (BonaPortableClass<T>) BonaPortableFactory.getBClassForPqon(pqon);
            return execute(ctx, request, bclass);
        } catch (final Exception e) {
            LOGGER.error("Error while determining the meta-class for PQON {}: {}", pqon, e.getMessage(), e);
            throw new T9tException(T9tAiException.INVALID_PQON, pqon, e.getMessage());
        }
    }
}
