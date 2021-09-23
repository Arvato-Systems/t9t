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
package com.arvatosystems.t9t.rest.vertx.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.rest.vertx.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.vertx.impl.ResponseFactory;
import com.arvatosystems.t9t.xml.GenericResult;
import com.arvatosystems.t9t.xml.Ping;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.vertx.core.Vertx;

/**
 * Ping endpoint. Handles a single PingRequest from the external API. This is
 * primarily used for authentication testing.
 */
@Path("ping")
@Singleton
public class PingRestResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingRestResource.class);

    @GET
    public Response testPingGet(@Context final HttpHeaders httpHeaders) {
        LOGGER.debug("Ping GET request at /ping");
        final GenericResult genericResponse = new GenericResult();
        final String acceptHeader = httpHeaders.getHeaderString("Accept");
        genericResponse.setReturnCode(0);
        return ResponseFactory.create(Response.Status.OK, genericResponse, acceptHeader);
    }

    @POST
    public Response testPingPost(@Context final HttpHeaders httpHeaders, Ping ping) {
        LOGGER.debug("Ping POST SYNC request at /ping for {}", ping);
        final GenericResult genericResponse = new GenericResult();
        final String acceptHeader = httpHeaders.getHeaderString("Accept");
        genericResponse.setReturnCode(0);
        return ResponseFactory.create(Response.Status.OK, genericResponse, acceptHeader);
    }

    @POST
    public void testPingPostAsync(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, Ping ping) {
        LOGGER.debug("Ping POST ASYNC request at /ping for {}", ping);
        final Vertx vertx = Jdp.getRequired(Vertx.class);
        Jdp.getRequired(Vertx.class).executeBlocking(
                x -> {
                    try {
                        LOGGER.debug("Ping POST ASYNC processing start");
                        Thread.sleep(1000L);
                        LOGGER.debug("Ping POST ASYNC processing end");
                    } catch (InterruptedException e) {
                        LOGGER.debug("Ping POST ASYNC processing exception");
                        e.printStackTrace();
                    }
                },
                x -> {
                    final GenericResult genericResponse = new GenericResult();
                    final String acceptHeader = httpHeaders.getHeaderString("Accept");
                    genericResponse.setReturnCode(0);
                    final Response resp1 = ResponseFactory.create(Response.Status.OK, genericResponse, acceptHeader);
                    resp.resume(resp1);
                });
    }
//    @POST
//    public void testAuthentication(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, Ping ping) {
//        LOGGER.debug("Ping POST request at /ping");
//
//        if (ping == null) {
//            GenericResult genericResponse = new GenericResult();
//            genericResponse.setErrorMessage("Invalid input");
//            genericResponse.setErrorDetails("The format of the request was invalid.");
//            genericResponse.setReturnCode(T9tException.CL_VALIDATION_ERROR * T9tException.CLASSIFICATION_FACTOR);
//            GenericResultFactory.returnAsyncResult(httpHeaders, resp, Response.Status.BAD_REQUEST, genericResponse);
//            return;
//        }
//
//        // we use PauseRequest instead of PingRequest because PauseRequest supports the pingId
//        PauseRequest pingRequest = new PauseRequest();
//        pingRequest.setPingId(ping.getPingId());
//
//        GenericResultFactory.performAsyncBackendRequest(httpHeaders, resp, pingRequest, "Test authentication.", PauseResponse.class, (PauseResponse sr) -> {
//            // after successful ping return the information in a GenericResponse
//            GenericResult genericResponse = GenericResultFactory.createResultFromServiceResponse(sr);
//            if (sr.getPingId() != null) {
//                genericResponse.setObjectRef(sr.getPingId().longValue());
//            }
//            return genericResponse;
//        });
//    }
}
