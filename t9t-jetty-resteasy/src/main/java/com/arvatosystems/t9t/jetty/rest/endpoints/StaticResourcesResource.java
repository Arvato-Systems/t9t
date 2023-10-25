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
package com.arvatosystems.t9t.jetty.rest.endpoints;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.LogSanitizer;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;

import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("")
@Singleton
public class StaticResourcesResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourcesResource.class);

    private static boolean enableSwagger = false;
    private static final Pattern PATH_PATTERN = Pattern.compile("[_A-Za-z0-9-]+[.][A-Za-z0-9]+"); // only simple filenames allowed

    public static void setEnableSwagger(final boolean enableSwagger) {
        StaticResourcesResource.enableSwagger = enableSwagger;
    }

    /**
     * Serving swagger-ui files
     */
    @GET
    @Path("{path: ^swagger-ui\\/.*}")
    @Operation(hidden = true)
    public Response swaggerUi(@PathParam("path") final String path) {
        if (!enableSwagger) {
            return Response.status(Status.FORBIDDEN).entity("Swagger not enabled").build();
        }
        if (!path.startsWith("swagger-ui/")) {
            // additional check
            return Response.status(Status.FORBIDDEN).entity("bad path").build();
        }
        if (!PATH_PATTERN.matcher(path.substring(11)).matches()) {
            // complain
            LOGGER.info("Refusing to serve {}", LogSanitizer.sanitize(path));
            return Response.status(Status.FORBIDDEN).entity("bad path argument").build();
        }

        final String absolutePath = "/" + path;
        LOGGER.debug("handling swaggerUi, absolutePath={}", absolutePath);

        final InputStream resource = StaticResourcesResource.class.getResourceAsStream(absolutePath);
        return resource == null
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.ok().entity(resource).build();
    }
}
