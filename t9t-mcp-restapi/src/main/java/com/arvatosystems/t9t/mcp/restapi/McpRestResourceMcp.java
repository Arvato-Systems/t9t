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
package com.arvatosystems.t9t.mcp.restapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestEndpointHandler;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.fasterxml.jackson.databind.JsonNode;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Singleton
@Path("/" + T9tAiMcpConstants.ENDPOINT_MCP)
public class McpRestResourceMcp implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpRestResourceMcp.class);

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);


    @Operation(summary = "MCP event stream",
            description = "Rejected - use POST for MCP requests",
            responses = { @ApiResponse(responseCode = "405", description = "Method not allowed.") })
    @Produces({ MediaType.SERVER_SENT_EVENTS })
    @GET
    public void mcpGet(@Context final HttpHeaders httpHeaders, @Context final SseEventSink eventSink, @Context final Sse sse) {
        LOGGER.debug("MCP connection attempted by GET /mcp - rejected");
        throw new WebApplicationException("Use POST", Response.Status.METHOD_NOT_ALLOWED);
    }

    @Operation(summary = "MCP request (streamable http)",
            description = "Initialize, list or call tools or prompts via MCP",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request processed successfully."),
                    @ApiResponse(responseCode = "202", description = "Confirm notifications."),
                    @ApiResponse(responseCode = "default", description = "Error processing request") })
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @POST
    public void mcpPost(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, final JsonNode body) {
        final String protocolVersion = httpHeaders.getHeaderString(T9tAiMcpConstants.HTTP_HEADER_MCP_PROTOCOL);
        final String sessionId = httpHeaders.getHeaderString(T9tAiMcpConstants.HTTP_HEADER_MCP_SESSION_ID);
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        final Object id = McpRestUtils.getId(body);
        final String method = McpRestUtils.getMethod(body);
        LOGGER.debug("MCP POST request received. Protocol Version: {}, Session-ID {}, Accept: {}, Id {}, Method {}", protocolVersion, sessionId, acceptHeader, id, method);
        // validate the method and protocol version. We have to respond with 400 if we do not support the requested protocol version.
        if (T9tUtil.isBlank(method)) {
            // method is required
            McpRestUtils.sendResponse(resp, Response.Status.BAD_REQUEST, "No method specified in request body");
            return;
        }
        if (protocolVersion != null && !McpRestUtils.SUPPORTED_PROTOCOL_VERSIONS.contains(protocolVersion)) {
            // we do not support the requested protocol version
            McpRestUtils.sendResponse(resp, Response.Status.BAD_REQUEST, "Unsupported MCP protocol version");
            return;
        }

        if (id == null) {
            // is a notification
            LOGGER.debug("MCP notification received for method {}", method);
            McpRestUtils.sendResponse(resp, Response.Status.ACCEPTED, null);
            return;
        }
        final IMcpRestEndpointHandler mcpRestEndpointHandler = Jdp.getOptional(IMcpRestEndpointHandler.class, method.toLowerCase());
        if (mcpRestEndpointHandler == null) {
            LOGGER.error("No handler found for method: {}", method);
            McpRestUtils.sendResponse(resp, Response.Status.BAD_REQUEST, mcpService.error(id, T9tAiMcpConstants.MCP_METHOD_NOT_FOUND, "Method not found: " + method));
            return;
        }
        mcpRestEndpointHandler.handleRequest(httpHeaders, resp, id, body);
    }
}
