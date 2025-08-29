package com.arvatosystems.t9t.mcp.restapi.service.impl;

import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpResultPayload;
import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.ai.request.AiGetToolsRequest;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named(McpUtils.METHOD_TOOLS_LIST)
public class ToolsListMcpRestRequestHandler implements IMcpRestRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsListMcpRestRequestHandler.class);

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Override
    public void handleRequest(@Nonnull final HttpHeaders httpHeaders, @Nonnull final AsyncResponse resp, @Nonnull final String id,
        @Nonnull final JsonNode body) {
        final AiGetToolsRequest toolsRequest = new AiGetToolsRequest();
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, toolsRequest, McpUtils.METHOD_TOOLS_LIST, AiGetToolsResponse.class,
            aiGetToolsResponse -> {
                LOGGER.debug("Retrieved {} tools", aiGetToolsResponse.getTools().size());
                final McpResultPayload mcpToolsResult = mcpService.mapGetToolsResponse(aiGetToolsResponse);
                return McpRestUtils.getJsonMediaData(mcpService.out(id, mcpToolsResult));
            }, sr -> mcpService.createMcpError(sr, id));
    }
}
