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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.arvatosystems.t9t.core.request.ExecutePreparedRequest;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Executes a canned request (needs additonal P. permissions)
 */
@Path("/run")
@Singleton
public class RunCannedRequestResource implements IT9tRestEndpoint {
    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @POST
    @Path("/{id}")
    public void runCannedRequestWithId(@Context final HttpHeaders httpHeaders, @PathParam("id") final String id, @Suspended final AsyncResponse resp) {
        final ExecutePreparedRequest requestParameters = new ExecutePreparedRequest();
        requestParameters.setRequestId(id);
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, requestParameters, "POST /runAsync");
    }
}
