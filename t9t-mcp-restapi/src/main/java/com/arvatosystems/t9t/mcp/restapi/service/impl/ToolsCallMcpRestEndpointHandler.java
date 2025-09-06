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
import com.arvatosystems.t9t.ai.request.AiRunToolRequest;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.mcp.restapi.McpRestUtils;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestEndpointHandler;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named(T9tAiMcpConstants.METHOD_TOOLS_CALL)
public class ToolsCallMcpRestEndpointHandler implements IMcpRestEndpointHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsCallMcpRestEndpointHandler.class);

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Override
    public void handleRequest(@Nonnull final HttpHeaders httpHeaders, @Nonnull final AsyncResponse resp, @Nonnull final String id,
        @Nonnull final JsonNode body) {
        final JsonNode paramNode = body.get(T9tAiMcpConstants.KEY_PARAMS);
        final String toolName = paramNode != null ? McpRestUtils.getName(paramNode) : null;
        final String arguments = paramNode != null ? McpRestUtils.getArgumentValue(paramNode) : null;
        LOGGER.debug("Received tools call request with toolName={}, arguments={}", toolName, arguments);
        if (T9tUtil.isBlank(toolName)) {
            LOGGER.error("Tool name is missing in the request body");
            McpRestUtils.sendResponse(resp, Response.Status.BAD_REQUEST, mcpService.error(id, T9tAiMcpConstants.MCP_INVALID_PARAMS, "Tool name is missing"));
            return;
        }
        final AiRunToolRequest runRq = new AiRunToolRequest();
        runRq.setName(toolName);
        runRq.setArguments(arguments);
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, runRq, T9tAiMcpConstants.METHOD_TOOLS_CALL, AiRunToolResponse.class,
            aiRunToolResponse -> {
                return McpRestUtils.getJsonMediaData(mcpService.out(id, mcpService.mapRunToolResponse(aiRunToolResponse)));
            }, sr -> mcpService.createMcpError(sr, id));
    }

}
