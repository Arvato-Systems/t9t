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

import de.jpaw.api.ConfigurationReader;
import de.jpaw.util.ConfigurationReaderFactory;


/**
 * Creates a HTTP Response with status and Payload
 */
public final class RestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    private RestUtils() { }

    public static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.restapi", null);

    public static boolean checkIfSet(final String configurationNameName, Boolean defaultValue) {
        final Boolean configuredValue = CONFIG_READER.getBooleanProperty(configurationNameName);
        if (configuredValue == null) {
            LOGGER.info("No value configured for {}, using default {}", configurationNameName, defaultValue);
            return defaultValue;
        } else {
            LOGGER.info("Configuration of {} is {}", configurationNameName, configuredValue);
            return configuredValue;
        }
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
