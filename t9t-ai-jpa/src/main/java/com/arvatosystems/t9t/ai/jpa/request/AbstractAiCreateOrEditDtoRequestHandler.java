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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiDtoAssistKey;
import com.arvatosystems.t9t.ai.AiResponseStructure;
import com.arvatosystems.t9t.ai.ClassWalker;
import com.arvatosystems.t9t.ai.JsonSchemaCreatorWithOpenAiWorkaround;
import com.arvatosystems.t9t.ai.JsonSchemaData;
import com.arvatosystems.t9t.ai.T9tAiException;
import com.arvatosystems.t9t.ai.jpa.entities.AiDtoAssistEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiAssistantDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiAssistantEntityResolver;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiDtoAssistEntityResolver;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaObject;
import com.arvatosystems.t9t.ai.request.AbstractAiCreateOrEditDtoRequest;
import com.arvatosystems.t9t.ai.request.AiCreateOrEditDtoResponse;
import com.arvatosystems.t9t.ai.request.ChatStatus;
import com.arvatosystems.t9t.ai.request.InternalResponseWrapper;
import com.arvatosystems.t9t.ai.service.IAiChatService;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.jackson.JacksonTools;

public abstract class AbstractAiCreateOrEditDtoRequestHandler<T extends BonaPortable, R extends AbstractAiCreateOrEditDtoRequest<T>> extends AbstractRequestHandler<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAiCreateOrEditDtoRequestHandler.class);
    private static final BonaPortableClass<?> WRAPPER_BCLASS = InternalResponseWrapper.BClass.INSTANCE;
    private static final Map<String, JsonSchemaObject> CACHED_SCHEMA_DATA = new ConcurrentHashMap<>(512);
    private static final ObjectMapper OBJECT_MAPPER = JacksonTools.createObjectMapper();
    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE_REF = new TypeReference<>() { };


    protected final IAiDtoAssistEntityResolver dtoAssistResolver = Jdp.getRequired(IAiDtoAssistEntityResolver.class);
    protected final IAiAssistantEntityResolver assistantResolver = Jdp.getRequired(IAiAssistantEntityResolver.class);
    protected final IAiAssistantDTOMapper assistantMapper = Jdp.getRequired(IAiAssistantDTOMapper.class);

    protected AiCreateOrEditDtoResponse<T> execute(final RequestContext ctx, final AbstractAiCreateOrEditDtoRequest<T> request, final BonaPortableClass<T> bclass) throws Exception {
        // read the configuration for the given DTO
        final AiDtoAssistEntity dtoAssist = dtoAssistResolver.getEntityData(new AiDtoAssistKey(bclass.getPqon()));

        // get the assistant configuration
        final AiAssistantDTO assistant = assistantMapper.mapToDto(dtoAssist.getAiAssistant());

        // Make redirection map (to avoid providing DTOs for all refs)
        final Map<String, String> redirectionMap = dtoAssist.getDtoSchema() == null ? Map.of() : OBJECT_MAPPER.readValue(dtoAssist.getDtoSchema(), STRING_MAP_TYPE_REF);

        // auto-generate JSON schema from class definition
        // initialize the required class tree
        final JsonSchemaData jsonSchemaData = ClassWalker.getSchemaData(bclass.getPqon());

        // validate the redirection data
        for (final var entry : redirectionMap.entrySet()) {
            final String refPqon = entry.getKey();
            final String targetPqon = entry.getValue();
            if (ClassWalker.getCachedSchemaData(refPqon) == null) {
                LOGGER.error("Redirection map contains reference PQON {} which is not part of the schema data for DTO {}", refPqon, bclass.getPqon());
            }
            if (ClassWalker.getCachedSchemaData(targetPqon) == null) {
                LOGGER.error("Redirection map contains target PQON {} which is not part of the schema data for DTO {}", targetPqon, bclass.getPqon());
            }
        }
        // jsonSchemaData.log(2);
        // generate the JSON schema for the DTO and the wrapper
        final JsonSchemaObject jsonSchemaObjOfDto = CACHED_SCHEMA_DATA.computeIfAbsent(bclass.getPqon(), x -> JsonSchemaCreatorWithOpenAiWorkaround.buildJsonSchemaObject(bclass.getMetaData(), null, true, true));
        final JsonSchemaObject jsonSchemaObjWrapper = JsonSchemaCreatorWithOpenAiWorkaround.buildJsonSchemaObject(WRAPPER_BCLASS.getMetaData(), "The desired DTO", true, false);
        // Log the raw wrapper
        // LOGGER.debug("Generated JSON schema for Wrapper: {}", ToStringHelper.toStringML(jsonSchemaObjWrapper));
        // link the DTO schema to the wrapper schema via $defs and $ref
        jsonSchemaObjWrapper.setDefs(JsonSchemaCreatorWithOpenAiWorkaround.createDefs(jsonSchemaData, redirectionMap));
        jsonSchemaObjWrapper.getProperties().put(InternalResponseWrapper.meta$$dto.getName(), jsonSchemaObjOfDto);

        // call the LLM service
        final IAiChatService chatService = Jdp.getRequired(IAiChatService.class, assistant.getAiProvider());

        final AiCreateOrEditDtoResponse<T> response = new AiCreateOrEditDtoResponse<>();

        try {
            final AiResponseStructure<InternalResponseWrapper<T>> aiResponse = chatService.chat2(ctx, assistant, request.getConversationId(), dtoAssist.getDtoInstructions(), request.getDto(), request.getChatInput(), jsonSchemaObjWrapper, null);

            // build response
            response.setConversationId(aiResponse.getConversationId());

            // extract the result from the AI response
            if (aiResponse.getResult() != null) {
                // only set the DTO if response message is not destroyed by an invalid object: check correctness here:
                final InternalResponseWrapper<T> resultWrapper = aiResponse.getResult();
                response.setChatStatus(resultWrapper.getChatStatus());
                response.setResponse(resultWrapper.getQuestionsOrSummary());
                response.setDto(resultWrapper.getDto());
                LOGGER.info("Successfully created/edited DTO of type {} via AI assistant", bclass.getPqon());
            } else {
                // no valid parsed result from LLM
                response.setResponse(aiResponse.getErrorMessage());
                response.setChatStatus(ChatStatus.NEED_MORE_INFORMATION);
                LOGGER.debug("No result in AI response for DTO {}", bclass.getPqon());
            }
        } catch (final Exception e) {
            // failed to parse - LLM might be asking for clarification
            response.setResponse("Failed to process AI response for DTO " + bclass.getPqon() + ", error: " + e.getMessage());
            response.setChatStatus(ChatStatus.NEED_MORE_INFORMATION);
            LOGGER.error("Failed to process AI response for DTO", e);
        }
        return response;
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
