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

import java.util.UUID;

import com.arvatosystems.t9t.msglog.request.QueryRequestResultRequest;
import com.arvatosystems.t9t.msglog.request.QueryRequestResultResponse;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.GenericResult;
import com.arvatosystems.t9t.xml.PreviousResult;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * Result check endpoint.
 */
@Tag(name = "t9t")
@Singleton
public class QueryResultRestResource implements IT9tRestEndpoint {

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Operation(
        summary = "Query result of a previous request",
        description = "The operation checks for completion of a previously issued asynchronous request.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Request passed.",
              content = @Content(schema = @Schema(implementation = PreviousResult.class))),
            @ApiResponse(responseCode = "default", description = "In case of error, a generic response with the error code is returned.",
            content = @Content(schema = @Schema(implementation = GenericResult.class))) })
    @GET
    @Path("queryResult/{messageId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void testPingPostAsync(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp,
      @PathParam("messageId") final UUID messageId) {
        checkNotNull(messageId, "messageId");
        final QueryRequestResultRequest rq = new QueryRequestResultRequest();
        rq.setMessageId(messageId);
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, rq, "POST /queryResult", QueryRequestResultResponse.class,
                r -> {
                    final PreviousResult qr = new PreviousResult();
                    if (ApplicationException.isOk(r.getReturnCode())) {
                        qr.setCompletedCheckedRequest(true);
                        qr.setReturnCodeOfCheckedRequest(r.getReturnCodeOfCheckedRequest());
                    } else {
                        qr.setCompletedCheckedRequest(false);
                    }
                    return qr;
                });
    }
}
