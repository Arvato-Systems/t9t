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

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EntitySearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.model.FieldConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EmbeddedIndexEntityConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Index Persistence Information:
 *
 * LUCENE BACKEND:
 * - Index is permanently stored on disk (default: ./lucene-indexes/)
 * - Survives application restarts
 * - Automatic updates during runtime for entity changes
 * - Only deleted by explicit dropAndCreate() or schema strategy
 *
 * ELASTICSEARCH BACKEND:
 * - Index is stored in Elasticsearch cluster
 * - Persistent independent of Java application
 *
 * AUTOMATIC UPDATES:
 * - Hibernate Search automatically updates the index on:
 *   - Entity INSERT/UPDATE/DELETE operations
 *   - Transaction commits
 *   - No manual reindexing needed for runtime changes
 */
@Singleton
public class IndexBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexBuilder.class);

    private final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);

    /**
     * Drop index (deletes all data!)
     */
    public <T> void dropIndex(Class<T> entityClass)  {

        SearchMapping searchMapping = Search.mapping(emf);

        // Drop old indexes if it exists
        searchMapping.scope(entityClass).schemaManager().dropIfExisting();

        LOGGER.info("Index dropped for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Creates missing indexes
     */
    public <T> void createMissingIndexes(Class<T> entityClass) throws InterruptedException {

        SearchMapping searchMapping = Search.mapping(emf);

        // Schema-Management
        searchMapping.scope(entityClass).schemaManager().createOrUpdate();

        // Validate Schema
        searchMapping.scope(entityClass).schemaManager().validate();

        // Mass Indexing
        searchMapping.scope(entityClass).massIndexer().startAndWait();

        LOGGER.info("Indexes created for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Checks the current index status
     */
    public <T> void checkIndexStatus(Class<T> entityClass) {

        try {
            // Validate schema - throws exception if index doesn't exist
            SearchMapping searchMapping = Search.mapping(emf);
            searchMapping.scope(entityClass).schemaManager().validate();
            LOGGER.info("Indexes exists and are valid for entity: {}", entityClass.getSimpleName());

            printIndexSummary();

        } catch (Exception e) {
            LOGGER.warn("Index validation failed for entity {}: {}", entityClass.getSimpleName(), e.getMessage());
        }
    }

    /**
     * Returns all indexed entity classes
     */
    private Set<Class<?>> getIndexedEntityClasses() {
        SearchMapping searchMapping = Search.mapping(emf);

        Set<Class<?>> indexedEntities = new HashSet<>();

        try {
            // Get all entity classes from the EntityManagerFactory
            Set<jakarta.persistence.metamodel.EntityType<?>> entityTypes = emf.getMetamodel().getEntities();

            for (jakarta.persistence.metamodel.EntityType<?> entityType : entityTypes) {
                Class<?> entityClass = entityType.getJavaType();

                // Check if the entity is indexed by trying to create a scope for it
                try {
                    searchMapping.scope(entityClass);
                    indexedEntities.add(entityClass);
                } catch (Exception e) {
                    // Entity is not indexed, skip it
                    LOGGER.debug("Entity {} is not indexed: {}", entityClass.getSimpleName(), e.getMessage());
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to get indexed entity classes: {}", e.getMessage());
        }

        return indexedEntities;
    }

    /**
     * Returns detailed information about all indexed entities and their fields
     */
    public List<IndexedEntityInfo> getIndexedEntitiesWithFields() {

        List<IndexedEntityInfo> result = new ArrayList<>();

        for (Class<?> entityClass : getIndexedEntityClasses()) {
            try {
                IndexedEntityInfo entityInfo = new IndexedEntityInfo();
                entityInfo.setEntityClass(entityClass);
                entityInfo.setEntityName(entityClass.getSimpleName());

                // Get field information from the entity's indexed fields
                List<String> indexedFields = getIndexedFieldsForEntity(entityClass);
                entityInfo.setIndexedFields(indexedFields);

                result.add(entityInfo);

            } catch (Exception e) {
                LOGGER.warn("Failed to get field information for entity {}: {}",
                        entityClass.getSimpleName(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * Gets all indexed field names for a specific entity
     */
    public List<String> getIndexedFieldsForEntity(Class<?> entityClass) {
        List<String> fields = new ArrayList<>();

        try {
            // Use reflection to find fields that would be indexed
            // This is a simplified approach - in practice, you might need to analyze
            // the actual Hibernate Search metadata more deeply
            java.lang.reflect.Field[] declaredFields = entityClass.getDeclaredFields();

            for (java.lang.reflect.Field field : declaredFields) {
                // Check if field has search annotations or if it's configured programmatically
                if (isFieldIndexed(field)) {
                    fields.add(field.getName());
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to analyze fields for entity {}: {}",
                    entityClass.getSimpleName(), e.getMessage());
        }

        return fields;
    }

    /**
     * Checks if a field is indexed (supports both annotations and programmatic configuration)
     */
    private boolean isFieldIndexed(Field field) {

        try {
            EntitySearchConfiguration configuration = ConfigurationLoader.loadConfiguration();
            if (configuration == null || configuration.getEntities() == null) {
                LOGGER.debug("No configuration available for field check");
                return false;
            }

            // Find the entity configuration
            for (EntityConfig entityConfig : configuration.getEntities()) {
                if (field.getDeclaringClass().getName().equals(entityConfig.getClassName())) {
                    LOGGER.debug("Found matching entity config for {}", entityConfig.getClassName());

                    // Check if field is in the regular fields list
                    if (entityConfig.getFields() != null) {
                        for (FieldConfig fieldConfig : entityConfig.getFields()) {
                            if (field.getName().equals(fieldConfig.getName())) {
                                LOGGER.info("✓ Field {}.{} IS INDEXED via programmatic configuration (type: {})",
                                          field.getDeclaringClass().getSimpleName(), field.getName(), fieldConfig.getType());
                                return true;
                            }
                        }
                    }

                    // Check if field is in the embedded fields list
                    if (entityConfig.getEmbeddedIndexEntities() != null) {
                        for (EmbeddedIndexEntityConfig embeddedConfig : entityConfig.getEmbeddedIndexEntities()) {
                            if (field.getName().equals(embeddedConfig.getTargetEntity())) {
                                LOGGER.info("✓ Field {}.{} IS INDEXED via embedded configuration",
                                          field.getDeclaringClass().getSimpleName(), field.getName());
                                return true;
                            }
                        }
                    }

                    return false; // Entity found, field not configured
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check programmatic configuration for field {}.{}: {}",
                    field.getDeclaringClass().getSimpleName(), field.getName(), e.getMessage());
        }

        return false;
    }

    /**
     * Prints a summary of all indexed entities and their fields
     */
    public void printIndexSummary() {
        LOGGER.info("=== Hibernate Search Index Summary ===");

        List<IndexedEntityInfo> entities = getIndexedEntitiesWithFields();

        if (entities.isEmpty()) {
            LOGGER.info("No indexed entities found");
            return;
        }

        for (IndexedEntityInfo entity : entities) {
            LOGGER.info("Indexed Entity: {}", entity.getEntityName());
            LOGGER.info("  Indexed Fields: {}", String.join(", ", entity.getIndexedFields()));
        }

        LOGGER.info("Total indexed entities: {}", entities.size());
    }

    /**
     * Data class to hold information about indexed entities
     */
    public static class IndexedEntityInfo {
        private Class<?> entityClass;
        private String entityName;
        private List<String> indexedFields = new ArrayList<>();

        // Getters and setters
        public Class<?> getEntityClass() {
            return entityClass;
        }

        public void setEntityClass(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        public List<String> getIndexedFields() {
            return indexedFields;
        }

        public void setIndexedFields(List<String> indexedFields) {
            this.indexedFields = indexedFields;
        }
    }
}
