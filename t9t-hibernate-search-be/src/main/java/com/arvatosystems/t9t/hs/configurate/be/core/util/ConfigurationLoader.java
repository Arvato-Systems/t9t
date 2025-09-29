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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.hs.T9tHibernateSearchException;
import com.arvatosystems.t9t.hs.configurate.model.EntitySearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.service.EntityConfigCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Utility class for loading Hibernate Search configuration.
 */
public final class ConfigurationLoader {

    private ConfigurationLoader() {
        // Utility class - private constructor to prevent instantiation
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);
    private static final String DEFAULT_CONFIG_FILE = "hibernate-search-entities.json";

    private static EntityConfigCache entityConfigCache;


    @Nonnull
    public static EntityConfigCache getEntityConfigCache() {
        return entityConfigCache;
    }

    /**
     * Loads the entity search configuration.
     */
    @Nonnull
    public static EntityConfigCache loadConfiguration() {
        return loadConfiguration(DEFAULT_CONFIG_FILE);
    }

    /**
     * Loads the entity search configuration from the specified JSON file.
     */
    @Nonnull
    public static EntityConfigCache loadConfiguration(@Nonnull final String configFileName) {
        try {
            // Return cached configuration if available
            if (entityConfigCache != null) {
                LOGGER.debug("Returning cached configuration");
                return entityConfigCache;
            }
            // Try to load from filesystem
            EntitySearchConfiguration config = tryLoadFromFileSystem(configFileName);

            // If not found, try to load from classpath
            if (config == null) {
                config = tryLoadFromClasspath(configFileName);
            }
            entityConfigCache = new EntityConfigCache(config != null ? config.getEntities() : Collections.emptyList());
            return entityConfigCache;
        } catch (final Exception e) {
            LOGGER.debug("Not a readable configuration '{}'", configFileName, e);
            throw new T9tException(T9tHibernateSearchException.HIBERNATE_SEARCH_INVALID_CONFIG, e.getMessage());
        }
    }

    @Nullable
    private static EntitySearchConfiguration tryLoadFromFileSystem(@Nonnull final String configFileName) throws IOException {

        final Path path = configFileName.startsWith("file:") ? Paths.get(URI.create(configFileName)) : Paths.get(configFileName);
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

    @Nullable
    private static EntitySearchConfiguration tryLoadFromClasspath(@Nonnull final String configFileName) throws IOException {
        try (InputStream inputStream = ConfigurationLoader.class.getClassLoader().getResourceAsStream(configFileName)) {
            if (inputStream == null) {
                LOGGER.warn("Configuration file {} not found in classpath", configFileName);
                return null;
            }
            return getEntitySearchConfiguration(inputStream);
        }
    }

    @Nonnull
    private static EntitySearchConfiguration getEntitySearchConfiguration(@Nonnull final InputStream inputStream) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final EntitySearchConfiguration configuration = objectMapper.readValue(inputStream, EntitySearchConfiguration.class);
        LOGGER.info("Loaded Hibernate Search configuration successfully.");
        LOGGER.debug("Loaded {} entities from configuration", configuration.getEntities() != null ? configuration.getEntities().size() : 0);
        return configuration;
    }
}
