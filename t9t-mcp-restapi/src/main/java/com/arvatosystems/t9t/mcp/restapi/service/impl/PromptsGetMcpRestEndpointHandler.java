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

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpPromptResult;
import com.arvatosystems.t9t.ai.request.AiGetPromptRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.mcp.restapi.McpRestUtils;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestEndpointHandler;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import de.jpaw.bonaparte.util.FreezeTools;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Singleton
@Named(T9tAiMcpConstants.METHOD_PROMPTS_GET)
public class PromptsGetMcpRestEndpointHandler implements IMcpRestEndpointHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptsGetMcpRestEndpointHandler.class);

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Override
    public void handleRequest(@Nonnull final HttpHeaders httpHeaders, @Nonnull final AsyncResponse resp, @Nonnull final String id,
        @Nonnull final JsonNode body) {
        final JsonNode paramNode = body.get(T9tAiMcpConstants.KEY_PARAMS);
        final String promptName = paramNode != null ? McpRestUtils.getName(paramNode) : null;
        final JsonNode argNode = paramNode != null ? paramNode.get(T9tAiMcpConstants.KEY_ARGUMENTS) : null;
        final int argCount = argNode != null ? argNode.size() : 0;
        LOGGER.debug("Received prompts get request for {} with {} arguments", promptName, argCount);
        final Map<String, String> arguments = convertArgumentsToMap(argNode);
        final AiGetPromptRequest promptRequest = new AiGetPromptRequest();
        promptRequest.setName(promptName);
        promptRequest.setArguments(arguments);
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, promptRequest, T9tAiMcpConstants.METHOD_PROMPTS_GET, AiGetPromptResponse.class,
            aiGetPromptResponse -> {
                final McpPromptResult result = mcpService.mapGetPromptResponse(aiGetPromptResponse);
                return McpRestUtils.getJsonMediaData(mcpService.out(id, result));
            }, sr -> mcpService.createMcpError(sr, id));
    }

    @Nonnull
    private Map<String, String> convertArgumentsToMap(@Nullable final JsonNode argNode) {
        if (argNode == null || !argNode.isObject()) {
            return new HashMap<>();
        }
        final Map<String, String> arguments = new HashMap<>(FreezeTools.getInitialHashMapCapacity(argNode.size()));
        for (Map.Entry<String, JsonNode> entry : argNode.properties()) {
            final String key = entry.getKey();
            final JsonNode valueNode = entry.getValue();
            if (valueNode != null && valueNode.isTextual()) {
                arguments.put(key, valueNode.asText());
            } else {
                LOGGER.warn("Ignoring non-textual argument value for key: {}, valueNode: {}", key, valueNode);
            }
        }
        return arguments;
    }
}
