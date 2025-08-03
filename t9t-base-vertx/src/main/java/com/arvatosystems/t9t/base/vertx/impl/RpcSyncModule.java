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
package com.arvatosystems.t9t.base.vertx.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.request.PingRequest;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.base.vertx.IServiceModule;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.api.codecs.IMessageDecoder;
import de.jpaw.bonaparte.api.codecs.IMessageEncoder;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteUtil;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * This is the same code as in /rpc and /rpcExt, but it will execute in the I/O thread, and not launch a separate worker,
 * in order to minimize latency caused by thread handover. It will be used for performance measurement and be removed
 * again if the benefit turns out to be neglectable.
 */
@Named("rpcSync")
@Dependent
public class RpcSyncModule implements IServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcSyncModule.class);

    protected static final PingRequest PING_REQUEST = new PingRequest();
    protected static final ServiceResponse RESPONSE = new ServiceResponse();

    static {
        RESPONSE.freeze();
    }

    private final ICachingAuthenticationProcessor authenticationProcessor = Jdp.getRequired(ICachingAuthenticationProcessor.class);
    private final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);

    @Override
    public String getModuleName() {
        return "rpcSync";
    }

    @Override
    public int getExceptionOffset() {
        return 10_000;
    }

    protected boolean withServiceRequest() {
        return false;
    }

    protected boolean skipAuthorization() {
        return false;
    }

    @Override
    public void mountRouters(final Router router, final Vertx vertx, final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        LOGGER.info("Registering module {}", getModuleName());
        router.post("/" + getModuleName()).handler((final RoutingContext ctx) -> {
            final long startInIoThread = System.nanoTime();

            final String origin = ctx.request().headers().get(HttpHeaders.ORIGIN);
            final MultiMap headers = ctx.request().headers();
            final String contentType = headers.get(HttpHeaders.CONTENT_TYPE);
            if (contentType == null) {
                LOGGER.debug("Request without Content-Type");
                IServiceModule.error(ctx, 415, "Content-Type not specified");
                return;
            }
            final String ct = HttpUtils.stripCharset(contentType);
            final String authHeader = headers.get(HttpHeaders.AUTHORIZATION);
            if (IServiceModule.badOrMissingAuthHeader(ctx, authHeader, LOGGER)) {
                return;
            }

            LOGGER.debug("POST /{} received for Content-Type {}", getModuleName(), ct);

            // check for valid en-/decoders for the payload
            final IMessageDecoder<BonaPortable, byte[]> decoder = coderFactory.getDecoderInstance(ct);
            final IMessageEncoder<ServiceResponse, byte[]> encoder = coderFactory.getEncoderInstance(ct);
            if (decoder == null || encoder == null) {
                IServiceModule.error(ctx, 415, "Content-Type «ct» not supported for path /" + getModuleName());
                return;
            }

            // =========================================================
            // start of modified code
            // =========================================================
            final AuthenticationInfo authInfo = authenticationProcessor.getCachedJwt(authHeader);
            if (authInfo.getEncodedJwt() == null) {
                // handle error
                IServiceModule.error(ctx, authInfo.getHttpStatusCode(),
                  authInfo.getMessage() + " " + ApplicationException.codeToString(T9tException.HTTP_ERROR + authInfo.getHttpStatusCode()));
                return;
            }
            // Authentication is valid. Now populate the MDC and start processing the request.
            final JwtInfo jwtInfo = authInfo.getJwtInfo();
            // Clear all old MDC data, since a completely new request is now processed
            MDC.clear();
            T9tInternalConstants.initMDC(jwtInfo);
            final Buffer buffer = ctx.body().buffer();
            final RequestParameters request;
            final ServiceResponse response;
            final long startProcessing = System.nanoTime();
            if (buffer == null) {
                request = PING_REQUEST;
                response = RESPONSE;
            } else {
                final byte[] body = buffer.getBytes();
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Request is:\n" + ByteUtil.dump(body, 1024)); // dump up to 1 KB of data
                }
                if (withServiceRequest()) {
                    final ServiceRequest rq = (ServiceRequest) decoder.decode(body, ServiceRequest.meta$$this);
                    LOGGER.debug("Received request {}, request length is {}", rq.ret$PQON(), body.length);
                    MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, rq.ret$PQON());
                    response = requestProcessor.execute(rq.getRequestHeader(), rq.getRequestParameters(), jwtInfo, authInfo.getEncodedJwt(),
                            skipAuthorization(), null);
                    request = rq.getRequestParameters();
                } else {
                    final RequestParameters rq = (RequestParameters) decoder.decode(body, ServiceRequest.meta$$requestParameters);
                    MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, rq.ret$PQON());
                    response = requestProcessor.execute(null, rq, jwtInfo, authInfo.getEncodedJwt(), skipAuthorization(), null);
                    request = rq;
                }
            }
            final byte[] respMsg = encoder.encode(response, ServiceResponse.meta$$this);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Response is:\n" + ByteUtil.dump(respMsg, 1024)); // dump up to 1 KB of data
            }
            final long endProcessing = System.nanoTime();
            if (ApplicationException.isOk(response.getReturnCode())) {
                LOGGER.info("Processed request {} for tenant {} in {}+{} us with result code {}, response length is {}", request.ret$PQON(),
                        jwtInfo.getTenantId(),
                        (startProcessing - startInIoThread) / 1000L,  // time for auth and MDC creation
                        (endProcessing - startProcessing) / 1000L,    // decoding request, executing it, encoding result
                        response.getReturnCode(),
                        respMsg.length);
            } else {
                LOGGER.debug("Processed request {} for tenant {} in {}+{} us with result code {}, error details {}, ({})", request.ret$PQON(),
                        jwtInfo.getTenantId(),
                        (startProcessing - startInIoThread) / 1000L,  // time for auth and MDC creation
                        (endProcessing - startProcessing) / 1000L,    // decoding request, executing it, encoding result
                        response.getReturnCode(),
                        response.getErrorDetails() == null ? "(null)" : response.getErrorDetails(),
                        ApplicationException.codeToString(response.getReturnCode()));
            }
            if (origin != null) {
                ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            }
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ct);
            ctx.response().end(Buffer.buffer(respMsg));
        });
    }
}
