/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.vertx

import com.arvatosystems.t9t.auth.be.vertx.T9tJwtAuthHandlerImpl
import com.arvatosystems.t9t.auth.be.vertx.T9tVertxUser
import com.arvatosystems.t9t.authc.api.GetTenantLogoRequest
import com.arvatosystems.t9t.authc.api.GetTenantLogoResponse
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.auth.PasswordAuthentication
import com.arvatosystems.t9t.base.types.SessionParameters
import com.arvatosystems.t9t.base.vertx.IServiceModule
import com.arvatosystems.t9t.server.services.IAuthenticate
import com.arvatosystems.t9t.server.services.IRequestProcessor
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory
import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.core.MessageParserException
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Dependent
import de.jpaw.dp.Inject
import de.jpaw.dp.Named
import de.jpaw.util.ApplicationException
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID

import static io.vertx.core.http.HttpHeaders.*
import static extension com.arvatosystems.t9t.base.vertx.impl.HttpUtils.*
import io.vertx.core.Handler
import io.vertx.core.AsyncResult

@AddLogger
@Named("auth")
@Dependent
class T9tAuthVertx extends T9tJwtAuthHandlerImpl implements IServiceModule {

    @Inject IAuthenticate authModule;
    @Inject IRequestProcessor requestProcessor;

    override getExceptionOffset() {
        return 1_000
    }

    override getModuleName() {
        return "auth"
    }

    def protected authByApiKeyAndStoreResult(String header) {
        var T9tVertxUser user = ACCESS_DENIED
        try {
            val uuid = UUID.fromString(header.substring(8).trim)
            val authResp = authModule.login(new AuthenticationRequest(new ApiKeyAuthentication(uuid)))
            if (authResp.returnCode == 0) {
                user = new T9tVertxUser(authResp.encodedJwt, authResp.jwtInfo)
            } else {
                LOGGER.info("Auth by API key rejected: Code {}: {} {}", authResp.returnCode, authResp.errorMessage, authResp.errorDetails)
            }
        } catch (Exception e) {
            LOGGER.info("Bad API Key auth: {}: {}", e.class.simpleName, e.message)
        }
        authCache.put(header, user)
        return user
    }

    def protected authBasicAndStoreResult(String header) {
        var T9tVertxUser user = ACCESS_DENIED
        try {
            val decoded = new String(Base64.urlDecoder.decode(header.substring(6).trim), StandardCharsets.UTF_8)
            val colonPos = decoded.indexOf(':')
            if (colonPos > 0 && colonPos < decoded.length) {
                val authResp = authModule.login(new AuthenticationRequest(new PasswordAuthentication(decoded.substring(0, colonPos), decoded.substring(colonPos+1))))
                if (authResp.returnCode == 0) {
                    user = new T9tVertxUser(authResp.encodedJwt, authResp.jwtInfo)
                } else {
                    LOGGER.info("Auth by Basic username / PW rejected: Code {}: {} {}", authResp.returnCode, authResp.errorMessage, authResp.errorDetails)
                }
            }
        } catch (Exception e) {
            LOGGER.info("Bad Basic auth: {}: {}", e.class.simpleName, e.message)
        }
        authCache.put(header, user)
        return user
    }

    def protected stripCharset(String s) {
        if (s === null)
            return s;
        val ind = s.indexOf(';')
        if (ind >= 0)
            return s.substring(0, ind)      // strip off optional ";Charset..." portion
        else
            return s
    }


    // hook for per-request basic or X509 authentification.
    // this is called while we actually wanted an existing token, but did not get one.
    // if we find another usable auth method, generate a temporary 1 minute token
    override authenticate(RoutingContext it, String header) {
        try {
            val ctx = it
            val Handler<AsyncResult<T9tVertxUser>> resultHandler = [
                if (succeeded && result.isOrWasValid) {
                    ctx.user = result
                    ctx.next
                } else {
                    ctx.error(403, "Authorization parameters not accepted")
                }]
            if (header.startsWith("Basic ")) {
                vertx.executeBlocking([ complete(authBasicAndStoreResult(header)) ], resultHandler)
                return
            }
            if (header.startsWith("API-Key ")) {
                vertx.executeBlocking([ complete(authByApiKeyAndStoreResult(header)) ], resultHandler)
                return
            }
        } catch (Exception e) {
            error(500, "http Authorization header processing exception")
            return
        }
        super.authenticate(it, header)
    }

    // setup sign-in and internal auth handlers
    override void mountRouters(Router router, Vertx vertx, IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        LOGGER.info("Registering module {}", moduleName)

        // add an OPTIONS handler
        router.addCorsOptionsRouter(moduleName)

        router.route("/api/*").handler(this);                           // authentication hook for /api/* calls
        router.route("/rpc").handler(this);                             // authentication hook for /rpc calls
        router.route("/rpcExt").handler(this);                          // authentication hook for /rpcExt calls
        router.post("/login").handler [                                             // create a new JWT token
            val origin = request.headers.get(ORIGIN)
            val ct = request.headers.get(CONTENT_TYPE)?.stripCharset
            LOGGER.info("Logging in for locale {}, content type {}, origin {}", preferredLocale?.language, ct, origin) // TODO switch to preferredLanguage()

            if (ct === null) {
                error(415, "Content-Type not specified for /login")
                return
            }
            // decode the payload
            val decoder = coderFactory.getDecoderInstance(ct)
            val encoder = coderFactory.getEncoderInstance(ct)
            if (decoder === null || encoder === null) {
                error(415, '''Content-Type «ct» not supported for path /login''')
                return
            }
            val ctx = it
            val session = new SessionParameters => [
                userAgent           = ctx.request.headers.get(USER_AGENT)
                locale              = ctx.preferredLocale?.language ?: "en"     // fix me: get BCP 47 string here
                dataUri             = ctx.request.remoteAddress?.host
            ]
            LOGGER.debug("Connection info is: remote address {}, user agent {}", session.dataUri, session.userAgent)
            vertx.<Void>executeBlocking([
                try {
                    val startTs  = System.currentTimeMillis
                    val request  = decoder.decode(ctx.body.bytes, AuthenticationRequest.meta$$this) as AuthenticationRequest
                    if (request.sessionParameters === null) {
                        request.sessionParameters = session     // set them completely
                    } else {
                        // just overwrite specific fields
                        val s = request.sessionParameters
                        if (s.userAgent === null)
                            s.userAgent = session.userAgent
                        if (s.locale === null)
                            s.locale = session.locale
                        if (s.dataUri === null)
                            s.dataUri = session.dataUri
                    }
                    val response = authModule.login(request)
                    if (response.returnCode == 0) {
                        response.tenantId = response.jwtInfo.tenantId
                    }
                    val respMsg  = encoder.encode(response, AuthenticationResponse.meta$$this)
                    val endTs    = System.currentTimeMillis
                    LOGGER.info("Processed /login in {} ms with result code {}, response length is {}", endTs - startTs, response.returnCode, respMsg.length)

                    if (origin !== null)
                        ctx.response.putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin)
                    ctx.response.putHeader(HttpHeaders.CONTENT_TYPE, ct)
                    ctx.response.end(Buffer.buffer(respMsg))
                } catch (MessageParserException e) {
                    ctx.error(400, e.message)
                } catch (Exception e) {
                    ctx.error(500, e.message)
                }
                complete()
            ], [])
        ]
        router.get("/api/authc/tenantLogo").handler [
            // request the tenant's logo from the DB
            LOGGER.debug("GET /api/authc/tenantLogo")

            // check authentication, before we do anything else
            val info = user?.principal?.map?.get("info")
            if (info === null || !(info instanceof JwtInfo)) {
                LOGGER.error("No user defined or of bad type: Missing auth handler for rpc?")
                error(500, "No user defined or of bad type: Missing auth handler?")
                return
            }
            val origin = request.headers.get(ORIGIN)
            val jwtInfo = info as JwtInfo
            val encodedJwt = user?.principal?.map?.get("jwt")
            val ctx = it
            vertx.<ServiceResponse>executeBlocking([
                try {
                    complete(requestProcessor.execute(null, new GetTenantLogoRequest, jwtInfo, encodedJwt as String, false));
                } catch (Exception e) {
                    LOGGER.info("{} in request: {}", e.class.simpleName, e.message)
                    fail(e)
                }
            ], [
                if (succeeded) {
                    if (result instanceof GetTenantLogoResponse) {
                        val r = result as GetTenantLogoResponse
                        val mediaType = MediaTypeInfo.getFormatByType(r.tenantLogo.mediaType)
                        if (mediaType !== null) {
                            if (origin !== null)
                                ctx.response.putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin)
                            ctx.response.putHeader(HttpHeaders.CONTENT_TYPE, mediaType.mimeType)
                            if (r.tenantLogo.text !== null)
                                ctx.response.end(r.tenantLogo.text)
                            else
                                ctx.response.end(Buffer.buffer(r.tenantLogo.rawData.bytes))
                        } else {
                            ctx.error(404)      // not found
                        }
                    } else {
                        ctx.error(500, result.errorMessage)
                    }
                } else {
                    val msg = if (cause instanceof ApplicationException)
                        cause.message + " " + (cause as ApplicationException).standardDescription
                    else
                        cause.message

                    if (cause instanceof MessageParserException) {
                        ctx.error(400, msg)
                    } else {
                        ctx.error(500, msg)
                    }
                }
            ])
        ]
    }
}
