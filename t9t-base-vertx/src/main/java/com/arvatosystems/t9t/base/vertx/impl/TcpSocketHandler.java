/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.request.ErrorRequest;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.bonaparte.core.ByteArrayComposer;
import de.jpaw.bonaparte.core.ByteArrayParser;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketHandler.class);
    private static final ServiceResponse NOT_AUTHENTICATED = new ServiceResponse();
    private static final ServiceResponse NOT_AUTHORIZED = new ServiceResponse();

    static {
        NOT_AUTHENTICATED.setReturnCode(T9tException.NOT_AUTHENTICATED);
        NOT_AUTHENTICATED.freeze();

        NOT_AUTHORIZED.setReturnCode(T9tException.NOT_AUTHORIZED);
        NOT_AUTHORIZED.freeze();
    }

    private final ByteBuilder inBuffer = new ByteBuilder(2000, StandardCharsets.UTF_8);
    private final NetSocket socket;
    private int numChunks;
    private String encodedJwt = null;
    private JwtInfo jwtInfo = null;

    private final IAuthenticate loginHandler = Jdp.getRequired(IAuthenticate.class);
    private final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);

    public TcpSocketHandler(final NetSocket socket) {
        this.socket = socket;
        LOGGER.info("New incoming socket connection from {}", socket.remoteAddress());
        numChunks = 0;

        socket.endHandler((final Void v) -> {
            // process the request if there is data
            if (inBuffer.length() > 0) {
                bufferComplete();
            }
        });
        socket.handler((final Buffer buffer) -> {
            final int len = buffer.length();
            inBuffer.require(len); // reserve space
            buffer.getBytes(inBuffer.getCurrentBuffer(), inBuffer.length());
            inBuffer.advanceBy(len);
            numChunks += 1;
            final byte[] buff = inBuffer.getCurrentBuffer();
            final int u = buff[inBuffer.length() - 1];
            if (u == 10) {
                // got a record
                bufferComplete();
            }
        });
        socket.closeHandler((final Void v) -> {
            LOGGER.info("Connection from {} is closed", socket.remoteAddress());
        });
    }

    protected ServiceResponse process(final RequestParameters rq) {
        if (rq instanceof AuthenticationRequest) {
            final AuthenticationResponse authResp = loginHandler.login((AuthenticationRequest) rq);
            if (authResp != null) {
                encodedJwt = authResp.getEncodedJwt();
                jwtInfo = authResp.getJwtInfo();
                return authResp;
            } else {
                encodedJwt = null;
                jwtInfo = null;
                return TcpSocketHandler.NOT_AUTHORIZED;
            }
        } else {
            if (encodedJwt == null) {
                if (rq instanceof ErrorRequest) {
                    // in case it is an error anyway, return just that error
                    final ErrorRequest errorRq = (ErrorRequest) rq;
                    final ServiceResponse resp = new ServiceResponse();
                    resp.setReturnCode(errorRq.getReturnCode());
                    resp.setErrorDetails(errorRq.getErrorDetails());
                    resp.setErrorMessage(ApplicationException.codeToString(errorRq.getReturnCode()));
                    return resp;
                }
                return NOT_AUTHENTICATED;
            }
            final ServiceResponse resp = requestProcessor.execute(null, rq, jwtInfo, encodedJwt, false, null);
            // check for switch tenant etc.
            if (resp instanceof AuthenticationResponse) {
                final AuthenticationResponse authResp = (AuthenticationResponse) resp;
                encodedJwt = authResp.getEncodedJwt();
                jwtInfo = authResp.getJwtInfo();
            }
            return resp;
        }
    }

    protected RequestParameters parse() {
        try {
            final ByteArrayParser bap = new ByteArrayParser(inBuffer.getCurrentBuffer(), 0, inBuffer.length());
            return (RequestParameters) bap.readRecord();
        } catch (ApplicationException e) {
            final ErrorRequest rq = new ErrorRequest();
            rq.setReturnCode(e.getErrorCode());
            rq.setErrorDetails(e.getMessage());
            return rq;
        } catch (Exception e) {
            final ErrorRequest rq = new ErrorRequest();
            rq.setReturnCode(T9tException.GENERAL_EXCEPTION);
            rq.setErrorDetails(e.getClass().getSimpleName() + ": " + e.getMessage());
            return rq;
        }
    }

    public Future<Void> bufferComplete() {
        LOGGER.debug("Processing TCP request of {} bytes length, received in {} chunks", inBuffer.length(), numChunks);
        numChunks = 0;
        final RequestParameters rq = parse();
        inBuffer.setLength(0);
        final ServiceResponse rs = process(rq);
        final ByteArrayComposer cbac = new ByteArrayComposer(); // TODO Resource leak: 'cbac' is never closed
        cbac.writeRecord(rs);
        final ByteBuf bb = Unpooled.wrappedBuffer(cbac.getBuffer(), 0, cbac.getLength());
        return socket.write(Buffer.buffer(bb));
    }
}
