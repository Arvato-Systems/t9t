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
package com.arvatosystems.t9t.rest.endpoints;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.request.PingRequest;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.xml.GenericResult;
import com.arvatosystems.t9t.xml.Ping;

import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Ping endpoint. Handles a single PingRequest from the external API. This is
 * primarily used for authentication testing.
 */
@Path("ping")
@Tag(name = "t9t")
@Singleton
public class PingRestResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingRestResource.class);

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Operation(
        summary = "Health check (quick)",
        description = "A successful GET confirms that the server is listening on the specified port.",
        responses = {
            @ApiResponse(
              description = "Request passed.",
              content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = GenericResult.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.")}
    )
    @GET
    public Response testPingGet(@Context final HttpHeaders httpHeaders) {
        LOGGER.debug("Ping GET request at /ping");
        return RestUtils.error(Response.Status.OK, 0, null, restProcessor.determineResponseType(httpHeaders));
    }

    @Operation(
        summary = "Health check (logged)",
        description = "A successful POST confirms that the server is listening on the specified port and the worker pool has available processing slots.",
        responses = {
            @ApiResponse(description = "Request passed.", content = @Content(schema = @Schema(implementation = GenericResult.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.")
        }
    )
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void testPingPostAsync(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, final Ping ping) {
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, new PingRequest(), "POST /ping");
    }

    @Operation(
        summary = "Health check (channeled)",
        description = "A successful GET sends a channeled ping to the server.",
        responses = {
            @ApiResponse(
              description = "Request passed.",
              content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = GenericResult.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.")}
    )
    @GET
    @Path("/{id}")
    public void testPong(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp,
            @Parameter(required = true, description = "ID.") @PathParam("id") final String id) {
        LOGGER.debug("Ping GET request at /ping/{id}");
        final String theId = T9tUtil.nvl(id, "0");
        final Ref dummy = new Ref();
        restProcessor.<Ref, PingRequest>performAsyncBackendRequestViaKafka(httpHeaders, resp, "GET /ping/{id}", List.of(dummy),
                s -> new PingRequest(), rq -> theId);
    }
}
