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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.arvatosystems.t9t.core.request.ExecutePreparedRequest;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Executes a canned request (needs additional P. permissions)
 */
@Path("run")
@Tag(name = "t9t")
@Singleton
public class RunCannedRequestResource implements IT9tRestEndpoint {
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Operation(
        summary = "Run a preconfigured request",
        description = "The request runs a request which has been preconfigured with all parameters.",
        responses = {
            @ApiResponse(description = "Request passed.",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResult.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.")}
    )
    @POST
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void runCannedRequestWithId(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp,
            @Parameter(required = true, description = "Request ID.") @PathParam("id") final String id) {
        final ExecutePreparedRequest requestParameters = new ExecutePreparedRequest();
        requestParameters.setRequestId(id);
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, requestParameters, "POST /run/" + id);
    }
}
