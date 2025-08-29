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
package com.arvatosystems.t9t.mcp.vertx.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.ai.request.AiGetSseRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.base.vertx.IServiceModule;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.ServerConfiguration;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

@Dependent
@Named(McpUtils.ENDPOINT_SSE)
public class McpSseModule implements IServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpSseModule.class);

    private record McpConnection(UUID id, HttpServerResponse response) { }

    private final ConcurrentHashMap<UUID, McpConnection> mcpConnections = new ConcurrentHashMap<>();
    private McpEndpointHandler endpointHandler;

    @Override
    public String getModuleName() {
        return "sse";
    }

    @Override
    public int getExceptionOffset() {
        return 10_000;
    }

    private final ICachingAuthenticationProcessor authenticationProcessor = Jdp.getRequired(ICachingAuthenticationProcessor.class);


    private void mcpMessage(final McpConnection connection, String event, JsonObject data) {
        mcpMessage(connection, event, data.encode());
    }

    private void mcpMessage(final McpConnection connection, String event, String data) {
        try {
            final String message = "event: " + event + "\ndata: " + data + "\n\n";
            connection.response.write(message);
        } catch (final Exception e) {
            LOGGER.error("Error sending SSE message: {}", e.getMessage());
            mcpConnections.remove(connection.id);
        }
      }

    @Override
    public void mountRouters(final Router router, final Vertx vertx, final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        final ServerConfiguration serverConfiguration = ConfigProvider.getConfiguration().getServerConfiguration();
        if (serverConfiguration == null || !Boolean.TRUE.equals(serverConfiguration.getSupportMcpSse())) {
            LOGGER.info("SSE endpoint is not enabled in server configuration, skipping module sse.");
            return;
        }
        final boolean heartbeat = Boolean.TRUE.equals(serverConfiguration.getSendHeartbeat());
        LOGGER.info("Registering module {}", getModuleName());
        endpointHandler = new McpEndpointHandler(vertx);

        // support for http GET method
        router.get("/" + McpUtils.ENDPOINT_SSE).handler(ctx -> {
            final MultiMap headers = ctx.request().headers();
            final String authHeader = headers.get(HttpHeaders.AUTHORIZATION);
            if (IServiceModule.badOrMissingAuthHeader(ctx, authHeader, LOGGER)) {
                return;
            }
            final AuthenticationInfo authInfo = authenticationProcessor.getCachedJwt(authHeader);
            if (authInfo.getEncodedJwt() == null) {
                // handle error
                LOGGER.debug("Not authenticated - JWT is null");
                IServiceModule.error(ctx, 401, "HTTP Authorization header missing or too short");
                return;
            }
            final String protocolVersion = headers.get(McpUtils.HTTP_HEADER_MCP_PROTOCOL);
            final String accept = headers.get(HttpHeaders.ACCEPT);
            LOGGER.debug("Received SSE GET request with protocol version: {}, accept: {}", protocolVersion, accept);

            final UUID connectionId = UUID.randomUUID();
            final AiGetSseRequest permCheck = new AiGetSseRequest();
            permCheck.setEssentialKey(connectionId.toString());
            // invoke check synchronously to ensure that the connection is allowed

            final HttpServerResponse response = ctx.response();

            response.putHeader("Content-Type", "text/event-stream")
              .putHeader("Cache-Control", "no-cache")
              .putHeader("Connection", "keep-alive")
              .putHeader("Access-Control-Allow-Origin", "*")
              .setChunked(true);

            final McpConnection connection = new McpConnection(connectionId, response);
            mcpConnections.put(connectionId, connection);

            LOGGER.info("SSE connection opened for: {} (by GET /sse)", connectionId);

            // Acknowledge connection
            // mcpMessage(connection, McpUtils.EVENT_CONNECTED, new JsonObject().put(McpUtils.KEY_CONNECTION_ID, connectionId));
            mcpMessage(connection, McpUtils.EVENT_ENDPOINT, "/sse");

            // keep connection alive
            final long heartbeatId;
            if (heartbeat) {
                heartbeatId = vertx.setPeriodic(30000, id -> {
                    if (mcpConnections.containsKey(connectionId)) {
                        mcpMessage(connection, McpUtils.EVENT_HEARTBEAT, new JsonObject().put(McpUtils.KEY_TIMESTAMP, System.currentTimeMillis()));
                    }
                });
            } else {
                heartbeatId = -1L; // No heartbeat timer
            }

            // Handle connection close
            response.closeHandler(v -> {
                if (heartbeatId != -1L) {
                    vertx.cancelTimer(heartbeatId);
                }
                mcpConnections.remove(connectionId);
                LOGGER.info("SSE connection closed: {}", connectionId);
            });

            // Handle client disconnect
            response.exceptionHandler(throwable -> {
                vertx.cancelTimer(heartbeatId);
                mcpConnections.remove(connectionId);
                LOGGER.error("SSE connection error: {}: {}", connectionId, throwable.getMessage());
            });
        });

        router.post("/" + McpUtils.ENDPOINT_SSE).handler(ctx -> {
            //final long startInIoThread = System.nanoTime();
            final MultiMap headers = ctx.request().headers();
            final String authHeader = headers.get(HttpHeaders.AUTHORIZATION);
            if (IServiceModule.badOrMissingAuthHeader(ctx, authHeader, LOGGER)) {
                return;
            }
            final String protocolVersion = headers.get(McpUtils.HTTP_HEADER_MCP_PROTOCOL);
            final String accept = headers.get(HttpHeaders.ACCEPT);
            LOGGER.debug("Received SSE POST request with protocol version: {}, accept: {}", protocolVersion, accept);

            vertx.<String>executeBlocking(() -> {
                try {
                    //final long startInWorkerThread = System.nanoTime();
                    // get the authentication info
                    final AuthenticationInfo authInfo = authenticationProcessor.getCachedJwt(authHeader);
                    if (authInfo.getEncodedJwt() == null) {
                        // handle error
                        throw new T9tException(T9tException.HTTP_ERROR + authInfo.getHttpStatusCode(), authInfo.getMessage());
                    }
                    // Authentication is valid. Now populate the MDC and start processing the request.
                    final JwtInfo jwtInfo = authInfo.getJwtInfo();
                    // Clear all old MDC data, since a completely new request is now processed
                    MDC.clear();
                    T9tInternalConstants.initMDC(jwtInfo);

                    final String response = endpointHandler.handleRequest(ctx.body(), authInfo, protocolVersion);
                    LOGGER.debug("Returning MCP response {}", response);
                    return response;
                } catch (final Exception e) {
                    LOGGER.error(e.getClass().getSimpleName() + " in request: " + e.getMessage(), e);
                    throw e;
                } finally {
                    // Clear the MDC after processing the request
                    MDC.clear();
                }
            }, false).onComplete((final AsyncResult<String> asyncResult) -> {
                if (asyncResult.succeeded()) {
                    final String result = asyncResult.result();
                    if (result != null) {
                        ctx.response()
                            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .end(result);
                    } else {
                        // notification do not want a response, so we return 204
                        LOGGER.debug("No response content for request, returning 204 No Content");
                        ctx.response()
                            .setStatusCode(204)
                            .end();
                    }
                } else {
                    final Throwable cause = asyncResult.cause();
                    if (cause instanceof ApplicationException ae) {
                        final int exceptionCode = ae.getErrorCode();
                        final int httpCode = (exceptionCode >= T9tException.HTTP_ERROR && exceptionCode <= T9tException.HTTP_ERROR + 999)
                            ? exceptionCode - T9tException.HTTP_ERROR
                            : 400;
                        final String msg = cause.getMessage() + " " + ((ApplicationException) cause).getStandardDescription();
                        ctx.response()
                            .setStatusCode(httpCode)
                            .setStatusMessage(msg)  // helpful error analysis
                            .end();
                    } else {
                        ctx.response()
                            .setStatusCode(500)
                            .setStatusMessage("Internal server error")  // do not leak information
                            .end();
                    }
                }
            });
        });
    }
}
