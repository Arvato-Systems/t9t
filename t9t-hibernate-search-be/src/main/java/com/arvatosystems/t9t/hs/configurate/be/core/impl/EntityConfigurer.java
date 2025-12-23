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

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.HibernateSearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.service.EntityConfigCache;
import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;
import com.arvatosystems.t9t.hs.configurate.model.EmbeddedIncludePathConfig;
import com.arvatosystems.t9t.hs.configurate.model.EmbeddedIndexEntityConfig;
import com.arvatosystems.t9t.hs.configurate.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.model.FieldConfig;
import de.jpaw.bonaparte.util.FreezeTools;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingConfigurationContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingStep;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * It loads the configuration from a JSON file and applies it programmatically.
     * EntityConfigurer.configure is automatically called by Hibernate Search during initialization
     * (see: com.arvatosystems.t9t.orm.jpa.hibernate.impl.EMFCustomizer).
     */
    @Override
    public void configure(@Nonnull final HibernateOrmMappingConfigurationContext context) {
        LOGGER.info("Starting Hibernate Search entity configuration");

        // Load configuration
        final EntityConfigCache configuration;
        if (hibernateSearchConfiguration == null || T9tUtil.isBlank(hibernateSearchConfiguration.getIndexConfigurationFile())) {
            LOGGER.info("No custom configuration file specified, using default configuration");
            configuration = ConfigurationLoader.loadConfiguration();
        } else {
            LOGGER.info("Loading Hibernate Search configuration from file: {}", hibernateSearchConfiguration.getIndexConfigurationFile());
            configuration = ConfigurationLoader.loadConfiguration(hibernateSearchConfiguration.getIndexConfigurationFile());
        }
        if (T9tUtil.isEmpty(configuration.getEntities())) {
            LOGGER.warn("No configuration found. Hibernate Search entity configuration will be skipped.");
            return;
        }
        final ProgrammaticMappingConfigurationContext mapping = context.programmaticMapping();

        // Configure each entity from the configuration file
        for (final EntityConfig entityConfig : configuration.getEntities()) {
            configureEntity(mapping, entityConfig);
        }

        LOGGER.info("Hibernate Search entity configuration completed for {} entities", configuration.getEntities().size());
    }

    /**
     * Configures a single entity based on the provided configuration.
     */
    private void configureEntity(@Nonnull final ProgrammaticMappingConfigurationContext mapping, @Nonnull final EntityConfig config) {
        LOGGER.debug("Configuring entity: {}", config.getClassName());

        try {
            // Create type mapping for the entity class
            final TypeMappingStep typeMapping = mapping.type(config.getClassName());

            // Apply @Indexed annotation if configured
            typeMapping.indexed();
            LOGGER.debug("@Indexed applied to entity: {}", config.getClassName());

            // Configure fields and collect search fields for full-text search with expression
            final int fieldSize = config.getFields() != null ? config.getFields().size() : 0;
            final int embeddedIndexSize = config.getEmbeddedIndexEntities() != null ? config.getEmbeddedIndexEntities().size() : 0;
            final int mapSize = FreezeTools.getInitialHashMapCapacity(fieldSize + embeddedIndexSize);
            final Set<String> searchKeywordNames = new HashSet<>(mapSize);
            final Map<Boolean, Set<String>> searchFullTextFieldNames = new HashMap<>(mapSize);
            if (config.getFields() != null) {
                for (FieldConfig fieldConfig : config.getFields()) {
                    configureField(typeMapping, fieldConfig, searchKeywordNames, searchFullTextFieldNames);
                }
            }
            // Embedded entities: collect includePaths into the same caches
            if (config.getEmbeddedIndexEntities() != null) {
                for (EmbeddedIndexEntityConfig embeddedConfig : config.getEmbeddedIndexEntities()) {
                    configureEmbeddedIndexEntity(typeMapping, embeddedConfig, searchKeywordNames, searchFullTextFieldNames);

                    String embeddedEntityClassName = resolveEmbeddedEntityClassName(config.getClassName(), embeddedConfig.getTargetEntity());
                    if (embeddedEntityClassName != null) {
                        configureEmbeddedEntityFields(mapping, embeddedEntityClassName, embeddedConfig);
                    }
                }
            }
            if (!searchFullTextFieldNames.isEmpty()) {
                CACHED_FULL_TEXT_FIELDS.put(config.getClassName(), searchFullTextFieldNames);
            }
            if (!searchKeywordNames.isEmpty()) {
                CACHED_KEYWORD_FIELDS.put(config.getClassName(), searchKeywordNames);
            }

        } catch (final Exception e) {
            LOGGER.error("Error configuring entity {}: {}", config.getClassName(), e.getMessage(), e);
        }
    }

    /**
     * Configures a field based on the field configuration.
     */
    private void configureField(@Nonnull final TypeMappingStep typeMapping, @Nonnull final FieldConfig config, @Nonnull final Set<String> searchKeywordNames,
        @Nonnull final Map<Boolean, Set<String>> searchFullTextFieldNames) {

        LOGGER.debug("Configuring field: {} with type: {}", config.getName(), config.getType());
        final PropertyMappingStep propertyStep = typeMapping.property(config.getName());

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
                final String sortField = config.getName() + SORT;
                final String analyser = config.getAnalyzer() != null ? config.getAnalyzer() : ANALYSER_FULLTEXT_STANDARD_TOKENIZER;

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
    private void configureEmbeddedIndexEntity(@Nonnull final TypeMappingStep typeMapping, @Nonnull final EmbeddedIndexEntityConfig config,
        @Nonnull final Set<String> searchKeywordNames, @Nonnull final Map<Boolean, Set<String>> searchFullTextFieldNames) {

        LOGGER.debug("Configuring embedded field: {}", config.getTargetEntity());
        try {
            var embedded = typeMapping.property(config.getTargetEntity()).indexedEmbedded();
            if (config.getIncludePaths() != null && !config.getIncludePaths().isEmpty()) {
                // Apply includePaths for indexing
                embedded.includePaths(config.getIncludePaths().stream().map(EmbeddedIncludePathConfig::getName).toArray(String[]::new));
                // Fill caches according to declared type
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
        } catch (final Exception e) {
            LOGGER.error("Error configuring embedded field {}: {}", config.getTargetEntity(), e.getMessage(), e);
        }
    }

    /**
     * Configures the fields of an embedded entity class, without marking it as @Indexed.
     * This is necessary for map value entities to ensure Hibernate Search can find their fields.
     */
    private void configureEmbeddedEntityFields(@Nonnull final ProgrammaticMappingConfigurationContext mapping,
        @Nonnull final String embeddedEntityClassName, @Nonnull final EmbeddedIndexEntityConfig config) {

        LOGGER.debug("Configuring fields for embedded entity class: {}", embeddedEntityClassName);
        try {
            // Create type mapping for the embedded entity class (but DON'T call .indexed())
            final TypeMappingStep embeddedTypeMapping = mapping.type(embeddedEntityClassName);

            // Initialize caches for embedded entity if they don't exist yet
            CACHED_KEYWORD_FIELDS.putIfAbsent(embeddedEntityClassName, new HashSet<>());
            final Set<String> embeddedKeywordFields = CACHED_KEYWORD_FIELDS.get(embeddedEntityClassName);

            CACHED_FULL_TEXT_FIELDS.putIfAbsent(embeddedEntityClassName, new HashMap<>());
            final Map<Boolean, Set<String>> embeddedFullTextFields = CACHED_FULL_TEXT_FIELDS.get(embeddedEntityClassName);

            // Configure fields from includePaths
            if (config.getIncludePaths() != null) {
                for (var fieldConfig : config.getIncludePaths()) {
                    if (fieldConfig.getName() == null || fieldConfig.getName().isEmpty()) continue;

                    LOGGER.debug("Configuring field: {} on embedded entity: {}", fieldConfig.getName(), embeddedEntityClassName);
                    final PropertyMappingStep propertyStep = embeddedTypeMapping.property(fieldConfig.getName());

                    switch (fieldConfig.getType().toLowerCase()) {
                        case "keywordfield":
                            CACHED_SORT_FIELDS.computeIfAbsent(fieldConfig.getName(), k -> fieldConfig.getName());
                            embeddedKeywordFields.add(fieldConfig.getName());
                            propertyStep.keywordField()
                                    .normalizer(ANALYSER_KEYWORD_NORMALIZER)
                                    .sortable(Sortable.YES);
                            break;
                        case "genericfield":
                            CACHED_SORT_FIELDS.computeIfAbsent(fieldConfig.getName(), k -> fieldConfig.getName());
                            propertyStep.genericField()
                                    .sortable(Sortable.YES);
                            break;
                        case "fulltextfield":
                        default:
                            final String sortField = fieldConfig.getName() + SORT;
                            final String analyzer = fieldConfig.getAnalyzer() != null ? fieldConfig.getAnalyzer() : ANALYSER_FULLTEXT_STANDARD_TOKENIZER;

                            CACHED_SORT_FIELDS.computeIfAbsent(fieldConfig.getName(), k -> fieldConfig.getName() + SORT);
                            embeddedKeywordFields.add(sortField);
                            embeddedFullTextFields
                                    .computeIfAbsent(ANALYSER_FUZZINESS.getOrDefault(analyzer, IS_FUZZY), k -> new HashSet<>())
                                    .add(fieldConfig.getName());

                            propertyStep.fullTextField()
                                    .analyzer(analyzer);
                            // Also add keyword field for sorting
                            embeddedTypeMapping.property(fieldConfig.getName())
                                    .keywordField(sortField)
                                    .normalizer(ANALYSER_KEYWORD_NORMALIZER)
                                    .sortable(Sortable.YES);
                            break;
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error configuring fields for embedded entity class {}: {}", embeddedEntityClassName, e.getMessage(), e);
        }
    }

    @Nullable
    private String resolveEmbeddedEntityClassName(@Nonnull final String parentEntityClassName, @Nonnull final String propertyName) {
        try {
            // Load the parent entity class
            final Class<?> parentClass = Class.forName(parentEntityClassName);

            // Try to find the property (getter method)
            final String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            final java.lang.reflect.Method getter = parentClass.getMethod(getterName);

            // Get the return type (should be a Map for our case)
            final java.lang.reflect.Type returnType = getter.getGenericReturnType();

            if (returnType instanceof java.lang.reflect.ParameterizedType paramType) {
                final java.lang.reflect.Type[] typeArguments = paramType.getActualTypeArguments();

                // For Map<String, EntityClass>, we want the second type argument (value type)
                if (typeArguments.length >= 2 && typeArguments[1] instanceof Class<?> valueClass) {
                    LOGGER.debug("Resolved embedded entity class for {}.{}: {}", parentEntityClassName, propertyName, valueClass.getName());
                    return valueClass.getName();
                }
            }

            LOGGER.warn("Could not resolve embedded entity class for {}.{}", parentEntityClassName, propertyName);
            return null;

        } catch (final Exception e) {
            LOGGER.warn("Failed to resolve embedded entity class for {}.{}: {}", parentEntityClassName, propertyName, e.getMessage());
            return null;
        }
    }
}
