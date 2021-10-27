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

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Encode / decoder to parse requests and send responses in JSON format (using jackson, plus addon modules).
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonObjectMapper implements ContextResolver<ObjectMapper> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonObjectMapper.class);

    private final ObjectMapper objectMapper;

    public JacksonObjectMapper() {
        this.objectMapper = new ObjectMapper();
        final boolean useNulls = checkIfSet("t9t.restapi.jsonAlsoNulls", "T9T_RESTAPI_JSON_ALSO_NULLS");
        if (!useNulls) {
            objectMapper.setSerializationInclusion(Include.NON_NULL);
        }
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        LOGGER.info("Jackson objectMapper: added Java8 date/time support, useNulls={}", useNulls);
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return objectMapper;
    }

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

}
