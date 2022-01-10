/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.request.PingRequest;
import com.arvatosystems.t9t.base.vertx.IServiceModule;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.api.codecs.IMessageDecoder;
import de.jpaw.bonaparte.api.codecs.IMessageEncoder;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public abstract class AbstractRpcModule implements IServiceModule {
    private static class WrappedResponse { // wrapper class, required to pass out data from lambdas
        private ServiceResponse response = AbstractRpcModule.RESPONSE;
      }

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcModule.class);

    protected static final PingRequest PING_REQUEST = new PingRequest();
    protected static final ServiceRequest PING_SRQ = new ServiceRequest(AbstractRpcModule.PING_REQUEST);
    protected static final ServiceResponse RESPONSE = new ServiceResponse();

    static {
        RESPONSE.freeze();
    }

    private final ICachingAuthenticationProcessor authenticationProcessor = Jdp.getRequired(ICachingAuthenticationProcessor.class);
    private final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);

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
            final long start = System.currentTimeMillis();

            final String origin = ctx.request().headers().get(HttpHeaders.ORIGIN);
            final String ct = ctx.request().headers().get(HttpHeaders.CONTENT_TYPE) == null ? null
                    : HttpUtils.stripCharset(ctx.request().headers().get(HttpHeaders.CONTENT_TYPE));
            if (ct == null) {
                LOGGER.debug("Request without Content-Type");
                IServiceModule.error(ctx, 415, "Content-Type not specified");
                return;
            }
            final String authHeader = ctx.request().headers().get(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || authHeader.length() < 8) {
                LOGGER.debug("Request without authorization header (length = {})", authHeader == null ? -1 : authHeader.length());
                IServiceModule.error(ctx, 401, "HTTP Authorization header missing or too short");
            }

            LOGGER.debug("POST /{} received for Content-Type {}", getModuleName(), ct);

            // check for valid en-/decoders for the payload
            final IMessageDecoder<BonaPortable, byte[]> decoder = coderFactory.getDecoderInstance(ct);
            final IMessageEncoder<ServiceResponse, byte[]> encoder = coderFactory.getEncoderInstance(ct);
            if (decoder == null || encoder == null) {
                IServiceModule.error(ctx, 415, "Content-Type «ct» not supported for path /" + getModuleName());
                return;
            }
            final WrappedResponse srq = new WrappedResponse(); // space to store the response for this request, defaulting to a "fast ping" response

            vertx.<byte[]>executeBlocking((final Promise<byte[]> promise) -> {
                try {
                    // Clear all old MDC data, since a completely new request is now processed
                    MDC.clear();
                    // get the authentication info
                    final AuthenticationInfo authInfo = authenticationProcessor.getCachedJwt(authHeader);
                    if (authInfo.getEncodedJwt() == null) {
                        // handle error
                        IServiceModule.error(ctx, authInfo.getHttpStatusCode(), authInfo.getMessage());
                        return;
                    }
                    // Authentication is valid. Now populate the MDC and start processing the request.
                    final JwtInfo jwtInfo = authInfo.getJwtInfo();
                    MDC.put(T9tConstants.MDC_USER_ID, jwtInfo.getUserId());
                    MDC.put(T9tConstants.MDC_TENANT_ID, jwtInfo.getTenantId());
                    MDC.put(T9tConstants.MDC_SESSION_REF, Objects.toString(jwtInfo.getSessionRef(), null));

                    final byte[] body = ctx.getBody() == null ? null : ctx.getBody().getBytes();
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Request is:\n" + ByteUtil.dump(body, 1024)); // dump up to 1 KB of data
                    }
                    final RequestParameters request;
                    if (body == null) {
                        request = PING_REQUEST;
                    } else {
                        if (withServiceRequest()) {
                            final ServiceRequest rq = (ServiceRequest) decoder.decode(body, ServiceRequest.meta$$this);
                            LOGGER.debug("Received request {}, request length is {}", rq.ret$PQON(), body.length);
                            MDC.put(T9tConstants.MDC_REQUEST_PQON, rq.ret$PQON());
                            srq.response = requestProcessor.execute(rq.getRequestHeader(), rq.getRequestParameters(), jwtInfo, authInfo.getEncodedJwt(),
                                    skipAuthorization());
                            request = rq.getRequestParameters();
                        } else {
                            final RequestParameters rq = (RequestParameters) decoder.decode(body, ServiceRequest.meta$$requestParameters);
                            MDC.put(T9tConstants.MDC_REQUEST_PQON, rq.ret$PQON());
                            srq.response = requestProcessor.execute(null, rq, jwtInfo, authInfo.getEncodedJwt(), skipAuthorization());
                            request = rq;
                        }
                    }
                    final ServiceResponse response = srq.response;
                    final byte[] respMsg = encoder.encode(response, ServiceResponse.meta$$this);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Response is:\n" + ByteUtil.dump(respMsg, 1024)); // dump up to 1 KB of data
                    }
                    final long end = System.currentTimeMillis();
                    if (ApplicationException.isOk(response.getReturnCode())) {
                        LOGGER.info("Processed request {} for tenant {} in {} ms with result code {}, response length is {}", request.ret$PQON(),
                                jwtInfo.getTenantId(), end - start, response.getReturnCode(), respMsg.length);
                    } else {
                        LOGGER.info("Processed request {} for tenant {} in {} ms with result code {}, error details {}, ({})", request.ret$PQON(),
                                jwtInfo.getTenantId(), end - start, response.getReturnCode(),
                                response.getErrorDetails() == null ? "(null)" : response.getErrorDetails(),
                                ApplicationException.codeToString(response.getReturnCode()));
                    }
                    promise.complete(respMsg);
                } catch (Exception e) {
                    LOGGER.error(e.getClass().getSimpleName() + " in requeste: " + e.getMessage(), e);
                    promise.fail(e);
                }
            }, false, (final AsyncResult<byte[]> asyncResult) -> {
                if (asyncResult.succeeded()) {
                    if (origin != null) {
                        ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                    }
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ct);
                    ctx.response().end(Buffer.buffer(asyncResult.result()));
                } else {
                    final String msg;
                    final Throwable cause = asyncResult.cause();
                    if (cause instanceof ApplicationException) {
                        msg = cause.getMessage() + " " + ((ApplicationException) cause).getStandardDescription();
                    } else {
                        msg = cause.getMessage();
                    }

                    if (cause instanceof ApplicationException) {
                        IServiceModule.error(ctx, 400, msg);
                    } else {
                        IServiceModule.error(ctx, 500, msg);
                    }
                }
            });
        });
    }
}
