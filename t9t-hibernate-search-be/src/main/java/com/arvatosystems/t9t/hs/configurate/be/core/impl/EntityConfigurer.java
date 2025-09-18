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
package com.arvatosystems.t9t.hs.configurate.be.core.impl;

import static com.arvatosystems.t9t.hs.configurate.be.core.constants.HsProperties.ANALYSER_FULLTEXT_STANDARD_TOKENIZER;
import static com.arvatosystems.t9t.hs.configurate.be.core.constants.HsProperties.ANALYSER_FUZZINESS;
import static com.arvatosystems.t9t.hs.configurate.be.core.constants.HsProperties.ANALYSER_KEYWORD_NORMALIZER;
import static com.arvatosystems.t9t.hs.configurate.be.core.constants.HsProperties.IS_FUZZY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingConfigurationContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingStep;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.HibernateSearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EmbeddedIndexEntityConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EntitySearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.model.FieldConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;

import jakarta.annotation.Nullable;

public class EntityConfigurer implements HibernateOrmSearchMappingConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityConfigurer.class);

    private static final Map<String, String> CACHED_SORT_FIELDS = new HashMap<>();
    private static final Map<String, Map<Boolean, Set<String>>> CACHED_FULL_TEXT_FIELDS = new HashMap<>();
    private static final Map<String, Set<String>> CACHED_KEYWORD_FIELDS = new HashMap<>();
    private static final String SORT = "_sort";

    @Nullable
    private final HibernateSearchConfiguration hibernateSearchConfiguration = ConfigProvider.getConfiguration().getHibernateSearchConfiguration();

    public static Map<String, String> getCachedSortFields() {
        return CACHED_SORT_FIELDS;
    }

    public static Map<String, Map<Boolean, Set<String>>> getCachedFullTextFields() {
        return CACHED_FULL_TEXT_FIELDS;
    }

    public static Map<String, Set<String>> getCachedKeywordFields() {
        return CACHED_KEYWORD_FIELDS;
    }

    /**
     * This method configures the Hibernate Search mapping for entity classes.
     * It loads configuration from a JSON file and applies it programmatically.
     * EntityConfigurer.configure is called automatically by Hibernate Search during initialization
     * (see: com.arvatosystems.t9t.orm.jpa.hibernate.impl.EMFCustomizer).
     */
    @Override
    public void configure(HibernateOrmMappingConfigurationContext context) {

        LOGGER.info("Starting Hibernate Search entity configuration");


        // Load configuration
        final EntitySearchConfiguration configuration;
        if (hibernateSearchConfiguration == null
                || hibernateSearchConfiguration.getIndexConfigurationFile() == null
                || hibernateSearchConfiguration.getIndexConfigurationFile().isEmpty()) {
            LOGGER.info("No custom configuration file specified, using default configuration");
            configuration = ConfigurationLoader.loadConfiguration();
        } else {
            LOGGER.info("Loading Hibernate Search configuration from file: {}", hibernateSearchConfiguration.getIndexConfigurationFile());
            configuration = ConfigurationLoader.loadConfiguration(hibernateSearchConfiguration.getIndexConfigurationFile());
        }
        if (configuration == null || configuration.getEntities() == null) {
            LOGGER.warn("No configuration found. Hibernate Search entity configuration will be skipped.");
            return;
        }

        final ProgrammaticMappingConfigurationContext mapping = context.programmaticMapping();

        // Configure each entity from the configuration file
        for (final EntityConfig entityConfig : configuration.getEntities()) {
            configureEntity(mapping, entityConfig);
        }

        LOGGER.info("Completed Hibernate Search entity configuration for {} entities", configuration.getEntities().size());

        // Note: Index summary logging moved to separate service to avoid EMF dependency during configuration
    }

    /**
     * Configures a single entity based on the provided configuration.
     */
    private void configureEntity(final ProgrammaticMappingConfigurationContext mapping, final EntityConfig config) {

        LOGGER.debug("Configuring entity: {}", config.getClassName());

        try {
            // Create type mapping for the entity class
            final TypeMappingStep typeMapping = mapping.type(config.getClassName());

            // Apply @Indexed annotation if configured
            typeMapping.indexed();
            LOGGER.debug("Applied @Indexed to entity: {}", config.getClassName());

            // Configure fields && collect search fields for full text search with expression
            final Set<String> searchKeywordNames = new HashSet<>();
            final Map<Boolean, Set<String>> searchFullTextFieldNames = new HashMap<>();
            if (config.getFields() != null) {
                for (FieldConfig fieldConfig : config.getFields()) {
                    configureField(typeMapping, fieldConfig, searchKeywordNames, searchFullTextFieldNames);
                }
            }
            // Embedded entities: collect includePaths into same caches
            if (config.getEmbeddedIndexEntities() != null) {
                for (EmbeddedIndexEntityConfig embeddedConfig : config.getEmbeddedIndexEntities()) {
                    configureEmbeddedIndexEntity(typeMapping, embeddedConfig, searchKeywordNames, searchFullTextFieldNames);
                }
            }
            if (!searchFullTextFieldNames.isEmpty()) {
                CACHED_FULL_TEXT_FIELDS.put(config.getClassName(), searchFullTextFieldNames);
            }
            if (!searchKeywordNames.isEmpty()) {
                CACHED_KEYWORD_FIELDS.put(config.getClassName(), searchKeywordNames);
            }

        } catch (final Exception e) {
            LOGGER.error("Failed to configure entity {}: {}", config.getClassName(), e.getMessage(), e);
        }
    }

    /**
     * Configures a field based on the field configuration.
     */
    private void configureField(final TypeMappingStep typeMapping, final FieldConfig config, final Set<String> searchKeywordNames, final Map<Boolean, Set<String>> searchFullTextFieldNames) {

        LOGGER.debug("Configuring field: {} with type: {}", config.getName(), config.getType());
        PropertyMappingStep propertyStep = typeMapping.property(config.getName());

        switch (config.getType().toLowerCase()) {
            case "keywordfield":
                CACHED_SORT_FIELDS.computeIfAbsent(config.getName(), k -> config.getName());
                searchKeywordNames.add(config.getName());
                propertyStep
                        .keywordField()
                        .normalizer(ANALYSER_KEYWORD_NORMALIZER)
                        .sortable(Sortable.YES);
                break;
            case "genericfield":
                CACHED_SORT_FIELDS.computeIfAbsent(config.getName(), k -> config.getName());
                propertyStep.genericField()
                        .sortable(Sortable.YES);
                break;
            case "fulltextfield":
            default:
                String sortField = config.getName() + SORT;
                String analyser = config.getAnalyzer() != null ? config.getAnalyzer() : ANALYSER_FULLTEXT_STANDARD_TOKENIZER;

                CACHED_SORT_FIELDS.computeIfAbsent(config.getName(), k -> config.getName() + SORT);
                searchKeywordNames.add(sortField);
                searchFullTextFieldNames
                        .computeIfAbsent(ANALYSER_FUZZINESS.getOrDefault(analyser, IS_FUZZY), k -> new HashSet<>())
                        .add(config.getName());

                propertyStep.fullTextField()
                        .analyzer(analyser);
                propertyStep
                        .keywordField(sortField)
                        .normalizer(ANALYSER_KEYWORD_NORMALIZER)
                        .sortable(Sortable.YES);
        }
    }

    /**
     * Configures an embedded field based on the embedded field configuration.
     */
    private void configureEmbeddedIndexEntity(final TypeMappingStep typeMapping, final EmbeddedIndexEntityConfig config,
                                              final Set<String> searchKeywordNames, final Map<Boolean, Set<String>> searchFullTextFieldNames) {

        LOGGER.debug("Configuring embedded field: {}", config.getTargetEntity());
        try {
            var embedded = typeMapping.property(config.getTargetEntity()).indexedEmbedded();
            if (config.getIncludePaths() != null && !config.getIncludePaths().isEmpty()) {
                // apply includePaths for indexing
                embedded.includePaths(config.getIncludePaths().stream().map(p -> p.getName()).toArray(String[]::new));
                // also populate caches according to declared type
                for (var includePath : config.getIncludePaths()) {
                    if (includePath.getName() == null || includePath.getName().isEmpty()) continue;
                    final String fieldname = config.getTargetEntity() + "." + includePath.getName();
                    switch (includePath.getType().toLowerCase()) {
                        case "keywordfield":
                            searchKeywordNames.add(fieldname);
                            break;
                        case "genericfield":
                            break;
                        case "fulltextfield":
                        default:
                            final String analyzer = includePath.getAnalyzer() != null ? includePath.getAnalyzer() : ANALYSER_FULLTEXT_STANDARD_TOKENIZER;
                            searchFullTextFieldNames
                                    .computeIfAbsent(ANALYSER_FUZZINESS.getOrDefault(analyzer, IS_FUZZY), k -> new HashSet<>())
                                    .add(fieldname);
                            break;
                    }
                }
            }
            embedded.indexingDependency().reindexOnUpdate(ReindexOnUpdate.SHALLOW);
        } catch (Exception e) {
            LOGGER.error("Failed to configure embedded field {}: {}", config.getTargetEntity(), e.getMessage(), e);
        }
    }
}
