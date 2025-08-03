package com.arvatosystems.t9t.mcp.restapi.service.impl;

import com.arvatosystems.t9t.ai.T9tAiConstants;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpResultPayload;
import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.ai.request.AiRunToolRequest;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.mcp.restapi.McpRestUtils;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestRequestHandler;
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
@Named(McpUtils.METHOD_TOOLS_CALL)
public class ToolsCallMcpRestRequestHandler implements IMcpRestRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsCallMcpRestRequestHandler.class);

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Override
    public void handleRequest(@Nonnull final HttpHeaders httpHeaders, @Nonnull final AsyncResponse resp, @Nonnull final String id,
        @Nonnull final JsonNode body) {
        final JsonNode paramNode = body.get(McpUtils.KEY_PARAMS);
        final String toolName = paramNode != null ? McpRestUtils.getName(paramNode) : null;
        final String arguments = paramNode != null ? McpRestUtils.getArgumentValue(paramNode) : null;
        LOGGER.debug("Received tools call request with toolName={}, arguments={}", toolName, arguments);
        if (T9tUtil.isBlank(toolName)) {
            LOGGER.error("Tool name is missing in the request body");
            McpRestUtils.sendResponse(resp, Response.Status.BAD_REQUEST, mcpService.error(id, T9tAiConstants.MCP_INVALID_PARAMS, "Tool name is missing"));
            return;
        }
        final AiRunToolRequest runRq = new AiRunToolRequest();
        runRq.setName(toolName);
        runRq.setArguments(arguments);
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, runRq, McpUtils.METHOD_TOOLS_CALL, AiRunToolResponse.class,
            aiRunToolResponse -> {
                final McpResultPayload mcpToolsResult = mcpService.mapRunToolsResponse(aiRunToolResponse);
                return McpRestUtils.getJsonMediaData(mcpService.out(id, mcpToolsResult));
            });
    }

}
