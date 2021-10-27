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
package com.arvatosystems.t9t.jetty.impl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Creates a HTTP Response with status and Payload
 */
public final class RestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    private RestUtils() { }

    private static boolean representsFalse(final char x) {
        return x == '0' || x == 'n' || x == 'N';
    }

    private static boolean isSet(final String value, final String byWhat) {
        if (value == null || value.length() == 0 || representsFalse(value.charAt(0))) {
            return false;
        }
        LOGGER.info("Property {} set (value {})", byWhat, value);
        return true;
    }

    public static boolean checkIfSet(final String systemPropertyName, final String envVariableName) {
        return isSet(System.getProperty(systemPropertyName), systemPropertyName) || isSet(System.getenv(envVariableName), envVariableName);
    }

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

    public static Response error(final Response.Status status) {
        final Response.ResponseBuilder response = Response.status(status);
        return response.build();
    }

    public static Response error(final Response.Status status, final String message) {
        final Response.ResponseBuilder response = Response.status(status);
        response.type(MediaType.TEXT_PLAIN_TYPE).entity(message);
        return response.build();
    }
}
