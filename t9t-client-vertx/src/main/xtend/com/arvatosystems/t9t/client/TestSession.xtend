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
package com.arvatosystems.t9t.client

import com.arvatosystems.t9t.base.T9tResponses
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.base.types.SessionParameters
import com.arvatosystems.t9t.base.vertx.impl.T9tServer
import com.arvatosystems.t9t.jdp.Init
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory
import de.jpaw.bonaparte.api.codecs.IMessageDecoder
import de.jpaw.bonaparte.api.codecs.IMessageEncoder
import de.jpaw.bonaparte.api.codecs.impl.SingleThreadCachingMessageCoderFactory
import de.jpaw.bonaparte.core.MimeTypes
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.unit.TestContext
import java.util.concurrent.CompletableFuture

@AddLogger
class TestSession {
    private String encodedJwt
    private final int port = 8024

    private final Vertx vertx = Vertx.vertx()
    private final IMessageCoderFactory<ServiceResponse, RequestParameters, byte[]> coderFactory = new SingleThreadCachingMessageCoderFactory(ServiceResponse, RequestParameters)
    private final String contentType
    private final IMessageDecoder<ServiceResponse, byte[]> decoder
    private final IMessageEncoder<RequestParameters, byte[]> encoder

    public new (String contentType) {
        this.contentType = contentType
        decoder = coderFactory.getDecoderInstance(contentType)
        encoder = coderFactory.getEncoderInstance(contentType)
        if (decoder === null || encoder === null)
            throw new RuntimeException("Cannot get encoder / decoder for " + contentType)
    }

    public new () {
        this(MimeTypes.MIME_TYPE_BONAPARTE)
    }

    def protected void setUp(TestContext context) {
        Init.initializeT9t
        vertx.deployVerticle(T9tServer.name, context.asyncAssertSuccess)
    }

    def protected void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess)
    }


    // for a session with successful login, execute a synchronous remote invocation
    def protected ServiceResponse exec(RequestParameters rp, String path, int expectedReturnCode) {
        val f = new CompletableFuture<Buffer>()
        val rpEncoded = encoder.encode(rp, ServiceRequest.meta$$requestParameters)
        vertx.createHttpClient => [
            val postRq = post(port, "localhost", path, [
                if (statusCode != 200) {
                    LOGGER.info('''Execution result for «rp.ret$PQON»: error code «statusCode», msg «statusMessage»''')
                    f.complete(null)
                } else {
                    LOGGER.info('''Request «rp.ret$PQON» OK''')
                    bodyHandler [
                        f.complete(it)
                    ]
                }
            ])
            if (encodedJwt !== null)
                postRq.putHeader(HttpHeaders.AUTHORIZATION, "Bearer " + encodedJwt)
            postRq.putHeader(HttpHeaders.CONTENT_TYPE, contentType)
            postRq.end(Buffer.buffer(rpEncoded))
        ]
        val encodedRes = f.get
        if (encodedRes === null) {
            LOGGER.error("No result for request {}", rp.ret$PQON)
            return T9tResponses.createServiceResponse(9999, "No response received")
        }
        val result = decoder.decode(encodedRes.bytes, ServiceResponse.meta$$this)
        if (result.returnCode != expectedReturnCode)
            throw new RuntimeException('''Bad return code for «rp.ret$PQON»: expected «expectedReturnCode», got «result.returnCode»''')
        return result
     }

     def protected JwtInfo login(AuthenticationParameters ap) {
        val ar = exec(new AuthenticationRequest => [
            authenticationParameters = ap
            sessionParameters = new SessionParameters => [
                dataUri = this.class.simpleName
                userAgent = "TestClient"
            ]
        ], "/login", 0) as AuthenticationResponse
        encodedJwt = ar.encodedJwt
        return ar.jwtInfo
    }

    def protected ServiceResponse exec(RequestParameters rp) {
        return exec(rp, "/rpc", 0)
    }
}
