/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.util.ConfigurationReaderFactory;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Encode / decoder to parse requests and send responses in JSON format (using jackson, plus addon modules).
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonObjectMapper implements ContextResolver<ObjectMapper> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonObjectMapper.class);

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

    private final ObjectMapper objectMapper;

    public JacksonObjectMapper() {
        final boolean useNulls = checkIfSet("t9t.restapi.jsonAlsoNulls", Boolean.FALSE);
        objectMapper = JacksonTools.createJacksonMapperForExports(useNulls);
        LOGGER.info("Jackson objectMapper: added Java8 date/time support, useNulls={}", useNulls);
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return objectMapper;
    }
}
