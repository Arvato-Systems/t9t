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
package com.arvatosystems.t9t.mcp.restapi.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpPromptResult;
import com.arvatosystems.t9t.ai.request.AiGetPromptRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.mcp.restapi.McpRestUtils;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestEndpointHandler;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.fasterxml.jackson.databind.JsonNode;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;

@Singleton
@Named(T9tAiMcpConstants.METHOD_PROMPTS_GET)
public class PromptsGetMcpRestEndpointHandler implements IMcpRestEndpointHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptsGetMcpRestEndpointHandler.class);

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Override
    public void handleRequest(final HttpHeaders httpHeaders, final AsyncResponse resp, final Object id, final JsonNode body) {
        final JsonNode paramNode = body.get(T9tAiMcpConstants.KEY_PARAMS);
        if (paramNode == null || !paramNode.isObject()) {
            LOGGER.error("Received prompts get request without parameters");
            throw new T9tException(T9tException.MISSING_DATA_PARAMETER);
        }
        final String promptName = McpRestUtils.getName(paramNode);
        final AiGetPromptRequest promptRequest = new AiGetPromptRequest();
        promptRequest.setName(promptName);

        final JsonNode argNode = paramNode.get(T9tAiMcpConstants.KEY_ARGUMENTS);
        if (argNode == null) {
            LOGGER.debug("Received prompts get request for {} without arguments", promptName);
            promptRequest.setArguments(new HashMap<>());
        } else {
            LOGGER.debug("Received prompts get request for {} with {} arguments", promptName, argNode.size());
            final Map<String, Object> arguments = mcpService.convertArgumentsToMap(argNode);
            promptRequest.setArguments(arguments);
        }
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, promptRequest, T9tAiMcpConstants.METHOD_PROMPTS_GET, AiGetPromptResponse.class,
            aiGetPromptResponse -> {
                final McpPromptResult result = mcpService.mapGetPromptResponse(aiGetPromptResponse);
                return McpRestUtils.getJsonMediaData(mcpService.out(id, result));
            }, sr -> mcpService.createMcpError(sr, id));
    }

//    @Nonnull
//    private Map<String, Object> convertArgumentsToMap(@Nullable final JsonNode argNode) {
//        final Map<String, Object> arguments = new HashMap<>(FreezeTools.getInitialHashMapCapacity(argNode.size()));
//        for (final Map.Entry<String, JsonNode> entry : argNode.properties()) {
//            final String key = entry.getKey();
//            final JsonNode valueNode = entry.getValue();
//            // convert to value
//            if (valueNode == null || valueNode.isNull()) {
//                arguments.put(key, null);
//            } else if (valueNode.isValueNode()) {
//                if (valueNode.isTextual()) {
//                    arguments.put(key, valueNode.asText());
//                } else if (valueNode.isBoolean()) {
//                    arguments.put(key, valueNode.asBoolean());
//                } else if (valueNode.isNumber()) {
//                    if (valueNode.isFloatingPointNumber()) {
//                        arguments.put(key, valueNode.asDouble());
//                    } else {
//                        arguments.put(key, valueNode.asLong());
//                    }
//                } else {
//                    LOGGER.warn("Ignoring non-textual argument value for key: {}, valueNode: {}", key, valueNode);
//                }
//            } else {
//                // object or array: needs to be implemented
//                LOGGER.warn("Ignoring container argument value for key: {}, valueNode: {}", key, valueNode);
//            }
//        }
//        return arguments;
//    }
}
