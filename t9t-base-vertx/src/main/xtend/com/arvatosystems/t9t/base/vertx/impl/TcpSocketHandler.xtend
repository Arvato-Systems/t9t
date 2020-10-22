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

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.request.ErrorRequest
import com.arvatosystems.t9t.server.services.IAuthenticate
import com.arvatosystems.t9t.server.services.IRequestProcessor
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.ByteArrayComposer
import de.jpaw.bonaparte.core.ByteArrayParser
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Inject
import de.jpaw.util.ApplicationException
import de.jpaw.util.ByteBuilder
import io.netty.buffer.Unpooled
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket
import java.nio.charset.StandardCharsets

@AddLogger
class TcpSocketHandler {
    static final ServiceResponse NOT_AUTHENTICATED = new ServiceResponse => [
        returnCode          = T9tException.NOT_AUTHENTICATED
        freeze
    ]
    static final ServiceResponse NOT_AUTHORIZED = new ServiceResponse => [
        returnCode          = T9tException.NOT_AUTHORIZED
        freeze
    ]
    final ByteBuilder   inBuffer = new ByteBuilder(2000, StandardCharsets.UTF_8)
    final NetSocket     socket;
    var int             numChunks;
    var String          encodedJwt = null
    var JwtInfo         jwtInfo = null

    @Inject IAuthenticate       loginHandler
    @Inject IRequestProcessor   requestProcessor

    def protected ServiceResponse process(RequestParameters rq) {
        if (rq instanceof AuthenticationRequest) {
            val authResp = loginHandler.login(rq)
            if (authResp !== null) {
                encodedJwt  = authResp.encodedJwt
                jwtInfo     = authResp.jwtInfo
                return authResp
            } else {
                encodedJwt  = null
                jwtInfo     = null
                return NOT_AUTHORIZED
            }
        } else {
            if (encodedJwt === null) {
                if (rq instanceof ErrorRequest)
                    // in case it is an error anyway, return just that error
                    return new ServiceResponse => [
                        returnCode      = rq.returnCode
                        errorDetails    = rq.errorDetails
                        errorMessage    = ApplicationException.codeToString(returnCode)
                    ]
                return NOT_AUTHENTICATED
            }
            val resp = requestProcessor.execute(null, rq, jwtInfo, encodedJwt, false)
            // check for switch tenant etc.
            if (resp instanceof AuthenticationResponse) {
                encodedJwt  = resp.encodedJwt
                jwtInfo     = resp.jwtInfo
            }
            return resp
        }
    }

    def protected RequestParameters parse() {
        try {
            //return CompactByteArrayParser.unmarshal(inBuffer.currentBuffer, ServiceRequest.meta$$requestParameters, RequestParameters)
            val bap = new ByteArrayParser(inBuffer.currentBuffer, 0, inBuffer.length)
            return bap.readRecord as RequestParameters
        } catch (ApplicationException e) {
            return new ErrorRequest => [
                returnCode      = e.errorCode
                errorDetails    = e.message
            ]
        } catch (Exception e) {
            return new ErrorRequest => [
                returnCode      = T9tException.GENERAL_EXCEPTION
                errorDetails    = e.class.simpleName + ": " + e.message
            ]
        }
    }

    def bufferComplete() {
            LOGGER.debug("Processing TCP request of {} bytes length, received in {} chunks", inBuffer.length, numChunks)
            numChunks = 0
            val rq = parse
            inBuffer.length = 0
            val rs = process(rq)
//            val outBuffer = new ByteBuilder(2000, StandardCharsets.UTF_8)
//            val cbac = new CompactByteArrayComposer(outBuffer, false)
//            cbac.addField(StaticMeta.OUTER_BONAPORTABLE, rs)
//            // wrap a ByteBuf around the response
//            val bb = Unpooled.wrappedBuffer(cbac.buffer, 0, cbac.length)
//            socket.write(Buffer.buffer(bb))
            val cbac = new ByteArrayComposer
            cbac.writeRecord(rs)
            val bb = Unpooled.wrappedBuffer(cbac.buffer, 0, cbac.length)
            socket.write(Buffer.buffer(bb))
    }

    new(NetSocket socket) {
        this.socket = socket
        LOGGER.info("New incoming socket connection from {}", socket.remoteAddress)
        numChunks = 0;

        socket.endHandler [
            // process the request if there is data
            if (inBuffer.length > 0)
                bufferComplete
        ]
        socket.handler [
            val len = length
            inBuffer.require(len)  // reserve space
            getBytes(inBuffer.currentBuffer, inBuffer.length)
            inBuffer.advanceBy(len)
            numChunks += 1
//            LOGGER.debug("   got {} bytes of data", len)
            val buff = inBuffer.currentBuffer
            val int u = buff.get(inBuffer.length-1)
            if (u == 10) {
                // got a record
                bufferComplete
            }
        ]
        socket.closeHandler [
            LOGGER.info("Connection from {} is closed", socket.remoteAddress)
        ]
    }
}
