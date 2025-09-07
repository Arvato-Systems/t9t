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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.base.vertx.IServiceModule;
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
import io.vertx.ext.web.Router;

@Dependent
@Named(T9tAiMcpConstants.ENDPOINT_MCP)
public class McpHttpModule implements IServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpHttpModule.class);

    private McpEndpointHandler endpointHandler;

    @Override
    public String getModuleName() {
        return T9tAiMcpConstants.ENDPOINT_MCP;
    }

    @Override
    public int getExceptionOffset() {
        return 10_000;
    }

    private final ICachingAuthenticationProcessor authenticationProcessor = Jdp.getRequired(ICachingAuthenticationProcessor.class);


    @Override
    public void mountRouters(final Router router, final Vertx vertx, final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        LOGGER.info("Registering module {}", getModuleName());
        endpointHandler = new McpEndpointHandler(null);

        // support for http GET method: Just deny it!
        router.get("/" + T9tAiMcpConstants.ENDPOINT_MCP).handler(ctx -> {
            // handle error
            LOGGER.debug("Received GET /mcp - rejecting");
            IServiceModule.error(ctx, 405, "Method not allowed - Use POST");
            return;
        });

        router.post("/" + T9tAiMcpConstants.ENDPOINT_MCP).handler(ctx -> {
            //final long startInIoThread = System.nanoTime();
            final MultiMap headers = ctx.request().headers();
            final String authHeader = headers.get(HttpHeaders.AUTHORIZATION);
            if (IServiceModule.badOrMissingAuthHeader(ctx, authHeader, LOGGER)) {
                return;
            }
            // obtain http headers
            final String protocolVersion = headers.get(T9tAiMcpConstants.HTTP_HEADER_MCP_PROTOCOL);
            final String sessionId = headers.get(T9tAiMcpConstants.HTTP_HEADER_MCP_SESSION_ID);
            final String accept = headers.get(HttpHeaders.ACCEPT);
            LOGGER.debug("Received MCP POST request with protocol version: {}, session ID {}, accept: {}", protocolVersion, sessionId, accept);

            vertx.<String>executeBlocking(() -> {
                try {
                    //final long startInWorkerThread = System.nanoTime();
                    // get the authentication info
                    final AuthenticationInfo authInfo = authenticationProcessor.getCachedJwt(MessagingUtil.massageAuthHeader(authHeader));
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
                            .putHeader(HttpHeaders.CONTENT_TYPE, T9tAiMcpConstants.HTTP_HEADER_MCP_JSON)
                            .end(result);
                    } else {
                        // notification do not want a response, so we return 202 (not 204, which is "no content"!)
                        LOGGER.debug("No response content for request, returning 202");
                        ctx.response()
                            .putHeader(HttpHeaders.CONTENT_TYPE, T9tAiMcpConstants.HTTP_HEADER_MCP_JSON)  // test
                            .setStatusCode(202)
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
