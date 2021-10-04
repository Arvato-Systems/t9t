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
package com.arvatosystems.t9t.jetty.rest.endpoints;

import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.InputStream;
import java.util.Objects;

@Path("")
@Singleton
public class StaticResourcesResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourcesResource.class);

    public static boolean enableSwagger = false;

    /**
     * Serving webjar dependencies
     */
    @GET
    @Path("{path: ^webjars\\/.*}")
    @Operation(hidden = true)
    public Response webjars(@PathParam("path") final String path) {
        if (!enableSwagger) {
            return Response.status(Status.FORBIDDEN).entity("Swagger not enabled").build();
        }
        LOGGER.debug("handling webjars: {}", path);
        String absolutePath = "/META-INF/resources/" + path;
        InputStream resource = StaticResourcesResource.class.getResourceAsStream(absolutePath);
        return Objects.isNull(resource)
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.ok().entity(resource).build();
    }

    /**
     * Serving static files from folders:
     *
     * /WEB-INF/resources
     * /WEB-INF/static
     * /WEB-INF/public
     * /WEB-INF/assets
     */
    @GET
    @Path("{path: ^(assets|public|static|resources)\\/.*}")
    @Operation(hidden = true)
    public Response staticResources(@PathParam("path") final String path) {
        if (!enableSwagger) {
            return Response.status(Status.FORBIDDEN).entity("Swagger not enabled").build();
        }
        LOGGER.debug("handling assets: {}", path);
        String absolutePath = "/WEB-INF/" + path;
        InputStream resource = StaticResourcesResource.class.getResourceAsStream(absolutePath);
        return null == resource
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.ok().entity(resource).build();
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
        String absolutePath = "/" + path;
        InputStream resource = StaticResourcesResource.class.getResourceAsStream(absolutePath);
        return null == resource
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.ok().entity(resource).build();
    }
}
