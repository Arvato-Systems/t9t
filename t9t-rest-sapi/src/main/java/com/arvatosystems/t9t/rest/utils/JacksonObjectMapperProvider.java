/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.rest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Encode / decoder to parse requests and send responses in JSON format (using jackson, plus addon modules).
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonObjectMapperProvider.class);

    private final ObjectMapper objectMapper;

    public JacksonObjectMapperProvider() {
        final boolean useNulls = RestUtils.checkIfSet("t9t.restapi.jsonAlsoNulls", Boolean.FALSE);
        final boolean failOnUnknownProperties = RestUtils.checkIfSet("t9t.restapi.jsonFailOnUnknownProperties", Boolean.FALSE);
        objectMapper = JacksonTools.createObjectMapper(useNulls, failOnUnknownProperties);
        LOGGER.info("Jackson objectMapper: added Java8 date/time support, useNulls={}, failOnUnknownProperties={}", String.valueOf(useNulls),
                String.valueOf(failOnUnknownProperties));
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return objectMapper;
    }
}
