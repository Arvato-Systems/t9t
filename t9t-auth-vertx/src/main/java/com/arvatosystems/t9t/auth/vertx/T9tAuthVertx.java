/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.vertx;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.authc.api.GetTenantLogoRequest;
import com.arvatosystems.t9t.authc.api.GetTenantLogoResponse;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.base.vertx.IServiceModule;
import com.arvatosystems.t9t.base.vertx.impl.HttpUtils;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.api.codecs.IMessageDecoder;
import de.jpaw.bonaparte.api.codecs.IMessageEncoder;
import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@Named("auth")
@Dependent
public class T9tAuthVertx implements IServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tAuthVertx.class);

    protected final IAuthenticate authModule = Jdp.getRequired(IAuthenticate.class);
    protected final ICachingAuthenticationProcessor authenticationProcessor = Jdp.getRequired(ICachingAuthenticationProcessor.class);
    protected final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);

    @Override
    public int getExceptionOffset() {
        return 1_000;
    }

    @Override
    public String getModuleName() {
        return "auth";
    }

    /**
        setup sign-in and internal auth handlers
     */
    @Override
    public void mountRouters(final Router router, final Vertx vertx, final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        LOGGER.info("Registering module {}", getModuleName());

        // add an OPTIONS handler
        HttpUtils.addCorsOptionsRouter(router, this.getModuleName());

        // create a new JWT token
        router.post("/login").handler(getLoginHandler(vertx, coderFactory));

        router.get("/api/authc/tenantLogo").handler(getTenantLogoHandler(vertx));
    }

    protected String stripCharset(final String s) {
        if (s == null) {
            return s;
        }
        final int ind = s.indexOf(";");
        if (ind >= 0) {
            // strip off optional ";Charset..." portion
            return s.substring(0, ind);
        } else {
            return s;
        }
    }

    private Handler<RoutingContext> getLoginHandler(final Vertx vertx, final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        return (final RoutingContext rc) -> {
            final String origin = rc.request().headers().get(HttpHeaders.ORIGIN);
            final String ct = stripCharset(rc.request().headers().get(HttpHeaders.CONTENT_TYPE));

            final String tag = rc.preferredLanguage() != null ? rc.preferredLanguage().tag() : null;
            LOGGER.info("Logging in for locale {}, content type {}, origin {}", tag, ct, origin);

            if (ct == null) {
                IServiceModule.error(rc, 415, "Content-Type not specified for /login");
                return;
            }

            // decode the payload
            final IMessageDecoder<BonaPortable, byte[]> decoder = coderFactory.getDecoderInstance(ct);
            final IMessageEncoder<ServiceResponse, byte[]> encoder = coderFactory.getEncoderInstance(ct);
            if (decoder == null || encoder == null) {
                final StringConcatenation builder = new StringConcatenation();
                builder.append("Content-Type ");
                builder.append(ct);
                builder.append(" not supported for path /login");
                IServiceModule.error(rc, 415, builder.toString());
                return;
            }

            final RoutingContext ctx = rc;

            String locale = null;
            if (ctx.preferredLanguage() != null && ctx.preferredLanguage().tag() != null) {
                locale = rc.preferredLanguage().tag();
            } else {
                // Default locale
                locale = "en";
            }

            final String dataUri = ctx.request().remoteAddress() != null ? ctx.request().remoteAddress().host() : null;

            final SessionParameters session = new SessionParameters();
            session.setUserAgent(ctx.request().headers().get(HttpHeaders.USER_AGENT));
            session.setLocale(locale); // fix me: get BCP 47 string here
            session.setDataUri(dataUri);

            LOGGER.debug("Connection info is: remote address {}, user agent {}", session.getDataUri(), session.getUserAgent());

            vertx.<Void>executeBlocking(
                promise -> {
                    try {
                        final long startTs = System.nanoTime();
                        final AuthenticationRequest request
                          = ((AuthenticationRequest) decoder.decode(ctx.body().buffer().getBytes(), AuthenticationRequest.meta$$this));

                        if (request.getSessionParameters() == null) {
                            // set them completely
                            request.setSessionParameters(session);
                        } else {
                            // just overwrite specific fields
                            final SessionParameters s = request.getSessionParameters();
                            if (s.getUserAgent() == null) {
                                s.setUserAgent(session.getUserAgent());
                            }
                            if (s.getLocale() == null) {
                                s.setLocale(session.getLocale());
                            }
                            if (s.getDataUri() == null) {
                                s.setDataUri(session.getDataUri());
                            }
                        }

                        final AuthenticationResponse response = this.authModule.login(request);
                        if (response.getReturnCode() == 0) {
                            response.setTenantId(response.getJwtInfo().getTenantId());
                        }

                        final byte[] respMsg = encoder.encode(response, AuthenticationResponse.meta$$this);
                        final long endTs = System.nanoTime();
                        LOGGER.info("Processed /login in {} us with result code {}, response length is {}",
                          (endTs - startTs) / 1000L, response.getReturnCode(), respMsg.length);

                        if (origin != null) {
                            ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                        }
                        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ct);
                        ctx.response().end(Buffer.buffer(respMsg));

                    } catch (final MessageParserException e) {
                        IServiceModule.error(ctx, 400, e.getMessage());
                    } catch (final Exception e) {
                        IServiceModule.error(ctx, 500, e.getMessage());
                    }
                    promise.complete();
                },
                asyncResult -> { }
            );
        };
    }

    private Handler<RoutingContext> getTenantLogoHandler(final Vertx vertx) {
        return (final RoutingContext rc) -> {
            // request the tenant's logo from the DB
            LOGGER.debug("GET /api/authc/tenantLogo");

            final String authHeader = rc.request().headers().get(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || authHeader.length() < 8) {
                LOGGER.debug("Request without authorization header (length = {})", authHeader == null ? -1 : authHeader.length());
                IServiceModule.error(rc, 401, "HTTP Authorization header missing or too short");
            }

            final String origin = rc.request().headers().get(HttpHeaders.ORIGIN);
            final RoutingContext ctx = rc;

            vertx.<ServiceResponse>executeBlocking(
                promise -> {
                    try {
                        // get the authentication info
                        final AuthenticationInfo authInfo = this.authenticationProcessor.getCachedJwt(authHeader);
                        if (authInfo.getEncodedJwt() == null) {
                            // handle error
                            IServiceModule.error(ctx, authInfo.getHttpStatusCode(), authInfo.getMessage());
                            return;
                        }

                        // Authentication is valid. Now populate the MDC and start processing the request.
                        final JwtInfo jwtInfo = authInfo.getJwtInfo();
                        promise.complete(this.requestProcessor.execute(null, new GetTenantLogoRequest(), jwtInfo, authInfo.getEncodedJwt(), false, null));

                    } catch (final Exception e) {
                        LOGGER.info("{} in request: {}", e.getClass().getSimpleName(), e.getMessage());
                        promise.fail(e);
                    }
                },
                asyncResult -> {
                    if (asyncResult.succeeded()) {
                        if (asyncResult.result() instanceof GetTenantLogoResponse r) {
                            final MediaTypeDescriptor mediaType = MediaTypeInfo.getFormatByType(r.getTenantLogo().getMediaType());
                            if (mediaType != null) {
                                if (origin != null) {
                                    ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                                }
                                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, mediaType.getMimeType());
                                if (r.getTenantLogo().getText() != null) {
                                    ctx.response().end(r.getTenantLogo().getText());
                                } else {
                                    ctx.response().end(Buffer.buffer(r.getTenantLogo().getRawData().getBytes()));
                                }
                            } else {
                                IServiceModule.error(ctx, 404);
                            }
                        } else {
                            IServiceModule.error(ctx, 500, asyncResult.result().getErrorMessage());
                        }
                    } else {
                        String msg = null;
                        final Throwable cause = asyncResult.cause();
                        if (cause instanceof ApplicationException apEx) {
                            msg = cause.getMessage() + apEx.getStandardDescription();
                        } else {
                            msg = cause.getMessage();
                        }

                        if (cause instanceof MessageParserException) {
                            IServiceModule.error(ctx, 400, msg);
                        } else {
                            IServiceModule.error(ctx, 500, msg);
                        }
                    }
                }
            );
        };
    }
}
