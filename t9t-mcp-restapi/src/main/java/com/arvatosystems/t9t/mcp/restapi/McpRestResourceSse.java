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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.request.AiGetSseRequest;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.mcp.restapi.service.IMcpRestEndpointHandler;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.fasterxml.jackson.databind.JsonNode;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nonnull;
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
@Path("/" + T9tAiMcpConstants.ENDPOINT_SSE)
public class McpRestResourceSse implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpRestResourceSse.class);

    private static final ScheduledExecutorService SHARED_SCHEDULER = Executors.newSingleThreadScheduledExecutor(
        r -> {
            final Thread t = new Thread(r, "t9t-SSE-Heartbeat");
            t.setDaemon(true);
            return t;
        }
    );

    protected final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Operation(summary = "SSE event stream",
            description = "Creates a connection for server-side events",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Connection established successfully."),
                    @ApiResponse(responseCode = "default", description = "Error occurred while establishing the connection.") })
    @Produces({ MediaType.SERVER_SENT_EVENTS })
    @GET
    public void sseGet(@Context final HttpHeaders httpHeaders, @Context final SseEventSink eventSink, @Context final Sse sse) {
        final String authHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
        }
        final String protocolVersion = httpHeaders.getHeaderString(T9tAiMcpConstants.HTTP_HEADER_MCP_PROTOCOL);
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        LOGGER.debug("SSE GET request received. Protocol Version: {}, Accept: {}", protocolVersion, acceptHeader);

        final UUID connectionId = UUID.randomUUID();
        final AiGetSseRequest permCheck = new AiGetSseRequest();
        permCheck.setEssentialKey(connectionId.toString());
        // invoke check synchronously to ensure that the connection is allowed
        final ServiceResponse response = restProcessor.performSyncBackendRequest(permCheck, authHeader, "GET /sse (permission check)");
        if (!ApplicationException.isOk(response.getReturnCode())) {
            LOGGER.error("Permission check failed for SSE connection: {} {}", response.getReturnCode(), response.getErrorMessage());
            throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
        }

        LOGGER.info("SSE connection opened for: {} (by GET /sse)", connectionId);

        try {
            eventSink.send(sse.newEvent(T9tAiMcpConstants.EVENT_CONNECTED, McpRestUtils.toJson(T9tAiMcpConstants.KEY_CONNECTION_ID, connectionId)));
            SHARED_SCHEDULER.scheduleAtFixedRate(getHeartBeatTask(eventSink, sse, connectionId.toString()),
                T9tAiMcpConstants.DEFAULT_HEARTBEAT_INTERVAL,
                T9tAiMcpConstants.DEFAULT_HEARTBEAT_INTERVAL,
                TimeUnit.MILLISECONDS);
        } catch (final Exception e) {
            LOGGER.error("Failed to establish SSE connection: {}", e.getMessage());
            if (!eventSink.isClosed()) {
                try {
                    eventSink.close();
                } catch (final IOException e1) {
                    LOGGER.error("Failed to close SSE connection: {}", e1.getMessage());
                }
            }
            throw new WebApplicationException("Failed to establish connection", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "MCP request - SSE",
            description = "Initialize, list or call tools or prompts via MCP",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request processed successfully."),
                    @ApiResponse(responseCode = "202", description = "Confirm notifications."),
                    @ApiResponse(responseCode = "default", description = "Error processing request") })
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @POST
    public void ssePost(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, final JsonNode body) {
        final String protocolVersion = httpHeaders.getHeaderString(T9tAiMcpConstants.HTTP_HEADER_MCP_PROTOCOL);
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        final Object id = McpRestUtils.getId(body);
        final String method = McpRestUtils.getMethod(body);
        LOGGER.debug("SSE POST request received. Protocol Version: {}, Accept: {}, Id {}, Method {}", protocolVersion, acceptHeader, id, method);
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
            LOGGER.debug("SSE notification received for method {}", method);
            McpRestUtils.sendResponse(resp, Response.Status.NO_CONTENT, null);
            return;
        }
        final IMcpRestEndpointHandler mcpRestEndpointHandler = Jdp.getOptional(IMcpRestEndpointHandler.class, method.toLowerCase());
        if (mcpRestEndpointHandler == null) {
            LOGGER.error("No handler found for method: {}", method);
            McpRestUtils.sendResponse(resp, Response.Status.NOT_FOUND, mcpService.error(id, T9tAiMcpConstants.MCP_METHOD_NOT_FOUND, "Method not found: " + method));
            return;
        }
        mcpRestEndpointHandler.handleRequest(httpHeaders, resp, id, body);
    }

    private Runnable getHeartBeatTask(@Nonnull final SseEventSink eventSink, @Nonnull final Sse sse, @Nonnull final String connectionId) {
        return new Runnable() {

            @Override
            public void run() {
                if (eventSink.isClosed()) {
                    LOGGER.debug("SSE Client disconnected: {}", connectionId);
                    stop();
                } else {
                    try {
                        LOGGER.debug("SSE Client {} is still open", connectionId);
                        eventSink.send(sse.newEvent(T9tAiMcpConstants.EVENT_HEARTBEAT, McpRestUtils.toJson(T9tAiMcpConstants.KEY_TIMESTAMP, System.currentTimeMillis())))
                            .thenAccept(result -> {
                                LOGGER.debug("Heartbeat sent to connection: {}", connectionId);
                            }).exceptionally(ex -> {
                                if (ex.getCause() instanceof IOException ioException) {
                                    LOGGER.debug("SSE Client {} disconnected: {}", connectionId, ioException.getMessage());
                                } else {
                                    LOGGER.error("Unexpected error sending heartbeat to {}: {}", connectionId, ex.getMessage());
                                }
                                stop();
                                return null;
                            });
                    } catch (Exception e) {
                        LOGGER.error("Error while sending heartbeat to {}: {}", connectionId, e.getMessage());
                        stop();
                    }
                }
            }

            private void stop() {
                try {
                    LOGGER.debug("Closing heartbeat task for connection: {}", connectionId);
                    if (!eventSink.isClosed()) {
                        eventSink.close();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error closing connection {}. Error {}", connectionId, e.getMessage());
                }
            }
        };
    }
}
