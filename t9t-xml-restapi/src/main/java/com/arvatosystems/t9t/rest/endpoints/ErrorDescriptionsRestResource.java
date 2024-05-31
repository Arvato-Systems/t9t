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
package com.arvatosystems.t9t.rest.endpoints;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.base.request.ErrorDescription;
import com.arvatosystems.t9t.base.request.RetrievePossibleErrorCodesRequest;
import com.arvatosystems.t9t.base.request.RetrievePossibleErrorCodesResponse;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.ErrorDescription001;
import com.arvatosystems.t9t.xml.ErrorDescriptionList;
import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * Endpoint retrieving the list of possible return codes.
 */
@Path("errordescriptions")
@Tag(name = "t9t")
@Singleton
public class ErrorDescriptionsRestResource implements IT9tRestEndpoint {

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Operation(
        summary = "Retrieve error descriptions",
        description = "A successful GET provides the list of error return codes and their descriptions.",
        responses = {
            @ApiResponse(responseCode = "200",
              description = "Request passed.",
              content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDescriptionList.class))),
            @ApiResponse(responseCode = "default", description = "In case of error, a generic response with the error code is returned.",
              content = @Content(schema = @Schema(implementation = GenericResult.class))) })
    @GET
    public void retrieveErrorCodes(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp) {
        final RetrievePossibleErrorCodesRequest rq = new RetrievePossibleErrorCodesRequest();
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, rq, "GET /errordescriptions", RetrievePossibleErrorCodesResponse.class,
                r -> {
                    final List<ErrorDescription001> results = new ArrayList<>(r.getErrorDescriptions().size());
                    for (final ErrorDescription desc : r.getErrorDescriptions()) {
                        results.add(
                                new ErrorDescription001(desc.getReturnCode(), desc.getErrorMessage(), desc.getApplicationLevel(), desc.getModuleDescription()));
                    }
                    return new ErrorDescriptionList(results);
                });
    }
}
