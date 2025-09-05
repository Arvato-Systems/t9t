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
package com.arvatosystems.t9t.hs.configurate.be.core.util;

import com.arvatosystems.t9t.hs.configurate.be.core.model.EntitySearchConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Utility class for loading Hibernate Search configuration.
 */
public final class ConfigurationLoader {

    private ConfigurationLoader() {
        // Utility class - private constructor to prevent instantiation
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);
    private static final String DEFAULT_CONFIG_FILE = "hibernate-search-entities.json";

    private static EntitySearchConfiguration cachedConfiguration;

    /**
     * Loads the entity search configuration.
     */
    public static EntitySearchConfiguration loadConfiguration() {

        return loadConfiguration(DEFAULT_CONFIG_FILE);
    }

    /**
     * Loads the entity search configuration from the specified JSON file.
     */
    public static EntitySearchConfiguration loadConfiguration(String configFileName) {

        if (cachedConfiguration != null) {
            LOGGER.debug("Returning cached configuration");
            return cachedConfiguration;
        }

        try (InputStream inputStream = ConfigurationLoader.class.getClassLoader()
                .getResourceAsStream(configFileName)) {

            if (inputStream == null) {
                LOGGER.warn("Configuration file {} not found in classpath", configFileName);
                return null;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            cachedConfiguration = objectMapper.readValue(inputStream, EntitySearchConfiguration.class);

            LOGGER.info("Successfully loaded Hibernate Search configuration from {} using Jackson", configFileName);
            LOGGER.debug("Loaded {} entities from configuration",
                    cachedConfiguration.getEntities() != null ? cachedConfiguration.getEntities().size() : 0);

            return cachedConfiguration;

        } catch (Exception e) {
            LOGGER.error("Failed to load configuration from {}: {}", configFileName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Clears the cached configuration (useful for testing).
     */
    public static void clearCache() {

        cachedConfiguration = null;
        LOGGER.debug("Configuration cache cleared");
    }
}
