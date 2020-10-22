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
package com.arvatosystems.t9t.base.vertx.impl

import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.base.vertx.IServiceModule
import com.arvatosystems.t9t.server.services.IRequestProcessor
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.core.MessageParserException
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Dependent
import de.jpaw.dp.Inject
import de.jpaw.dp.Named
import de.jpaw.util.ApplicationException
import de.jpaw.util.ByteUtil
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router

import static io.vertx.core.http.HttpHeaders.*

import static extension com.arvatosystems.t9t.base.vertx.impl.HttpUtils.*
import com.arvatosystems.t9t.base.T9tConstants
import org.slf4j.MDC
import java.util.Objects

/** This is a pseudo module in the sense that rather than contributing a specific set of methods,
 * it provides a dispatcher.
 */
@Named("rpc")
@Dependent
class RpcModule extends AbstractRpcModule {
    override getModuleName() {
        return "rpc"
    }
    override skipAuthorization() {  // used by internal callers only
        return true
    }
}

/** This is a pseudo module in the sense that rather than contributing a specific set of methods,
 * it provides a dispatcher.
 */
@Named("rpcExt")
@Dependent
class RpcExtModule extends AbstractRpcModule {
    override getModuleName() {
        return "rpcExt"
    }
}

@AddLogger
abstract class AbstractRpcModule implements IServiceModule {
    static protected final PingRequest PING_REQUEST = new PingRequest
    static protected final ServiceRequest  PING_SRQ = new ServiceRequest(PING_REQUEST)
    static protected final ServiceResponse RESPONSE = new ServiceResponse => [ freeze ]

    static private class WrappedResponse {      // wrapper class, required to pass out data from lambdas
        ServiceResponse response = RESPONSE
    }
    @Inject IRequestProcessor requestProcessor;

    override getExceptionOffset() {
        return 10_000
    }

    def protected boolean withServiceRequest() {
        return false;
    }

    def protected boolean skipAuthorization() {
        return false;
    }

    override void mountRouters(Router router, Vertx vertx, IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        LOGGER.info("Registering module {}", moduleName)

        // add an OPTIONS handler - see CorsModule!
        // router.addCorsOptionsRouter(moduleName)

        router.post("/" + moduleName).handler [
            val start = System.currentTimeMillis

            // Clear all old MDC data, since complete new request if processed
            MDC.clear

            val origin = request.headers.get(ORIGIN)
            val ct = request.headers.get(CONTENT_TYPE).stripCharset
            if (ct === null) {
                error(415, "Content-Type not specified")
                return
            }
            LOGGER.debug("POST /{} received for Content-Type {}", getModuleName(), ct)

            // check authentication, before we do anything else
            val info = user?.principal?.map?.get("info")
            if (info === null || !(info instanceof JwtInfo)) {
                LOGGER.error("No user defined or of bad type: Missing auth handler for rpc?")
                error(500, "No user defined or of bad type: Missing auth handler?")
                return
            }
            val jwtInfo = info as JwtInfo
            val encodedJwt = user?.principal?.map?.get("jwt")

            MDC.put(T9tConstants.MDC_USER_ID, jwtInfo.getUserId)
            MDC.put(T9tConstants.MDC_TENANT_ID, jwtInfo.getTenantId)
            MDC.put(T9tConstants.MDC_SESSION_REF, Objects.toString(jwtInfo.getSessionRef, null))

            // decode the payload
            val decoder = coderFactory.getDecoderInstance(ct)
            val encoder = coderFactory.getEncoderInstance(ct)
            if (decoder === null || encoder === null) {
                error(415, '''Content-Type «ct» not supported for path /«getModuleName()»''')
                return
            }
            val srq = new WrappedResponse  // space to store the response for this request, defaulting to a "fast ping" response
            val ctx = it
            val mdcContext = MDC.copyOfContextMap
            vertx.<byte []>executeBlocking([
                try {
                    MDC.setContextMap(mdcContext)
                    val body = ctx.body?.bytes
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("Request is:\n" + ByteUtil.dump(body, 1024))  // dump up to 1 KB of data
                    val request = if (body === null) PING_REQUEST else {
                        if (withServiceRequest) {
                            val rq = decoder.decode(body, ServiceRequest.meta$$this) as ServiceRequest;
                            LOGGER.debug("Received request {}, request length is {}", rq.ret$PQON, body.length);
                            MDC.put(T9tConstants.MDC_REQUEST_PQON, rq.ret$PQON)
                            srq.response = requestProcessor.execute(rq.requestHeader, rq.requestParameters, jwtInfo, encodedJwt as String, skipAuthorization);
                            rq.requestParameters
                        } else {
                            val rq = decoder.decode(body, ServiceRequest.meta$$requestParameters) as RequestParameters;
                            MDC.put(T9tConstants.MDC_REQUEST_PQON, rq.ret$PQON)
                            srq.response = requestProcessor.execute(null, rq, jwtInfo, encodedJwt as String, skipAuthorization);
                            rq
                        }
                    }
                    val response = srq.response  // unwrap
                    val respMsg  = encoder.encode(response, ServiceResponse.meta$$this)
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("Response is:\n" + ByteUtil.dump(respMsg, 1024))  // dump up to 1 KB of data
                    val end = System.currentTimeMillis
                    if (ApplicationException.isOk(response.returnCode))
                        LOGGER.info("Processed request {} for tenant {} in {} ms with result code {}, response length is {}",
                            request.ret$PQON, jwtInfo.tenantId, end - start, response.returnCode, respMsg.length
                        )
                    else
                        LOGGER.info("Processed request {} for tenant {} in {} ms with result code {}, error details {}, ({})",
                            request.ret$PQON, jwtInfo.tenantId, end - start, response.returnCode, response.errorDetails ?: "(null)",
                            ApplicationException.codeToString(response.returnCode)
                        )
                    complete(respMsg)
                } catch (Exception e) {
                    LOGGER.error('''«e.class.simpleName» in request: «e.message»''', e)
                    fail(e)
                }
            ], false, [
                MDC.setContextMap(mdcContext)
                if (succeeded) {
                    if (origin !== null)
                        ctx.response.putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin)
                    ctx.response.putHeader(CONTENT_TYPE, ct)
                    ctx.response.end(Buffer.buffer(result))
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
