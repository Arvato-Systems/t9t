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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public static EntitySearchConfiguration loadConfiguration(final String configFileName) {
        try {
            // Return cached configuration if available
            if (cachedConfiguration != null) {
                LOGGER.debug("Returning cached configuration");
                return cachedConfiguration;
            }
            // Try to load from filesystem
            EntitySearchConfiguration loaded = tryLoadFromFileSystem(configFileName);

            // If not found, try to load from classpath
            if (loaded == null) {
                loaded = tryLoadFromClasspath(configFileName);
            }
            cachedConfiguration = loaded;
            return loaded;
        } catch (Exception e) {
            LOGGER.debug("Not a readable configuration '{}': {}", configFileName, e.toString());
        }
        return null;
    }

    private static EntitySearchConfiguration tryLoadFromFileSystem(String configFileName) throws IOException {

        Path path;
        if (configFileName.startsWith("file:")) {
            path = Paths.get(URI.create(configFileName));
        } else {
            path = Paths.get(configFileName);
        }
        if (path.isAbsolute()) {
            if (!Files.exists(path)) {
                LOGGER.warn("Configuration file {} not found on filesystem", path);
                return null;
            }
            try (InputStream is = Files.newInputStream(path)) {
                return getEntitySearchConfiguration(is);
            }
        }
        return null;
    }

    private static EntitySearchConfiguration tryLoadFromClasspath(String configFileName) throws IOException {
        try (InputStream inputStream = ConfigurationLoader.class.getClassLoader().getResourceAsStream(configFileName)) {
            if (inputStream == null) {
                LOGGER.warn("Configuration file {} not found in classpath", configFileName);
                return null;
            }
            return getEntitySearchConfiguration(inputStream);
        }
    }

    private static EntitySearchConfiguration getEntitySearchConfiguration(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        EntitySearchConfiguration configuration = objectMapper.readValue(inputStream, EntitySearchConfiguration.class);
        LOGGER.info("Loaded Hibernate Search configuration successfully.");
        LOGGER.debug("Loaded {} entities from configuration", configuration.getEntities() != null ? configuration.getEntities().size() : 0);
        return configuration;
    }

    /**
     * Clears the cached configuration (useful for testing).
     */
    public static void clearCache() {
        cachedConfiguration = null;
        LOGGER.debug("Configuration cache cleared");
    }
}
