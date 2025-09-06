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
import com.arvatosystems.t9t.ai.mcp.McpInitializeResult;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestEndpointHandler;
import com.fasterxml.jackson.databind.JsonNode;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@Singleton
@Named(T9tAiMcpConstants.METHOD_INITIALIZE)
public class InitializeMcpRestEndpointHandler implements IMcpRestEndpointHandler {

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);

    @Override
    public void handleRequest(@Nonnull final HttpHeaders httpHeaders, @Nonnull final AsyncResponse resp, @Nonnull final String id,
        @Nonnull final JsonNode body) {
        final String protocolVersion = httpHeaders.getHeaderString(T9tAiMcpConstants.HTTP_HEADER_MCP_PROTOCOL);
        final McpInitializeResult result = mcpService.getInitializeResult(T9tUtil.nvl(protocolVersion, T9tAiMcpConstants.FALLBACK_MCP_PROTOCOL_VERSION), "t9t jetty REST gateway");
        final String output = mcpService.out(id, result);
        resp.resume(Response.ok(output).build());
    }
}
