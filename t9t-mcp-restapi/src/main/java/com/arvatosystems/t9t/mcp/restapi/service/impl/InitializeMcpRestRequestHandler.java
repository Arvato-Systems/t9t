package com.arvatosystems.t9t.mcp.restapi.service.impl;

import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpInitializeResult;
import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestRequestHandler;
import com.fasterxml.jackson.databind.JsonNode;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@Singleton
@Named(McpUtils.METHOD_INITIALIZE)
public class InitializeMcpRestRequestHandler implements IMcpRestRequestHandler {

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);

    @Override
    public void handleRequest(@Nonnull final HttpHeaders httpHeaders, @Nonnull final AsyncResponse resp, @Nonnull final String id,
        @Nonnull final JsonNode body) {
        final String protocolVersion = httpHeaders.getHeaderString(McpUtils.HTTP_HEADER_MCP_PROTOCOL);
        final McpInitializeResult result = mcpService.getInitializeResult(T9tUtil.nvl(protocolVersion, McpUtils.FALLBACK_MCP_PROTOCOL_VERSION));
        final String output = mcpService.out(id, result);
        resp.resume(Response.ok(output).build());
    }
}
