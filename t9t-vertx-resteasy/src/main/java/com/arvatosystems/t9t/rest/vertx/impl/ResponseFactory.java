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
package com.arvatosystems.t9t.rest.vertx.impl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.HttpRequest;


/**
 * Creates a HTTP Response with status and Payload
 * @author LUEC034
 */
public class ResponseFactory {

    public static Response create(final Response.Status status, final Object payload, final String acceptHeader) {
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

    @Deprecated
    public static Response create(final Response.Status status, final Object payload, final HttpRequest context) {
        final Response.ResponseBuilder response = Response.status(status);
        response.entity(payload);

        if (payload instanceof String) {
            response.type(MediaType.TEXT_PLAIN_TYPE);
        } else {
            final String acceptHeader = context.getHttpHeaders().getHeaderString("Accept");
            if (acceptHeader != null) {
                response.type(acceptHeader);
            } else {
                response.type(MediaType.APPLICATION_XML);
            }
        }
        return response.build();
    }

    public static Response create(final Response.Status status) {
        final Response.ResponseBuilder response = Response.status(status);
        return response.build();
    }

    public static Response error(final Response.Status status) { // maybe extend for better error handling, i.e. parsing internal status codes to HTTP status codes.
        final Response.ResponseBuilder response = Response.status(status);
        return response.build();
    }

    public static Response error(final Response.Status status, final String message) {
        final Response.ResponseBuilder response = Response.status(status);
        response.type(MediaType.TEXT_PLAIN_TYPE).entity(message);
        return response.build();
    }
}
