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
package com.arvatosystems.t9t.rest.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.request.PingRequest;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.GenericResult;
import com.arvatosystems.t9t.xml.Ping;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Ping endpoint. Handles a single PingRequest from the external API. This is
 * primarily used for authentication testing.
 */
@Path("ping")
@Singleton
public class PingRestResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingRestResource.class);

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @GET
    public Response testPingGet(@Context final HttpHeaders httpHeaders) {
        LOGGER.debug("Ping GET request at /ping");
        final GenericResult genericResponse = new GenericResult();
        final String acceptHeader = httpHeaders.getHeaderString("Accept");
        genericResponse.setReturnCode(0);
        return createResponse(Response.Status.OK, genericResponse, acceptHeader);
    }

//    @POST
//    public Response testPingPost(@Context final HttpHeaders httpHeaders, Ping ping) {
//        LOGGER.debug("Ping POST SYNC request at /ping for {}", ping);
//        final GenericResult genericResponse = new GenericResult();
//        final String acceptHeader = httpHeaders.getHeaderString("Accept");
//        genericResponse.setReturnCode(0);
//        return createResponse(Response.Status.OK, genericResponse, acceptHeader);
//    }

    private static Response createResponse(final Response.Status status, final Object payload, final String acceptHeader) {
        final Response.ResponseBuilder response = Response.status(status);
        response.entity(payload);

        if (payload instanceof String) {
            response.type(MediaType.TEXT_PLAIN_TYPE);
        } else {
            if (acceptHeader != null) {
                response.type(acceptHeader);
            } else {
                response.type(MediaType.APPLICATION_XML);
            }
        }
        return response.build();
    }

    @POST
    public void testPingPostAsync(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, Ping ping) {
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, new PingRequest(), "POST /ping");
    }
}
