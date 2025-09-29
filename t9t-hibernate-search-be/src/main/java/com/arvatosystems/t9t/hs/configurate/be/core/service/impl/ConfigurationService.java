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
package com.arvatosystems.t9t.hs.configurate.be.core.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.hs.configurate.be.core.service.EntityConfigCache;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.hibernate.search.mapper.orm.schema.management.SearchSchemaManager;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.hs.configurate.be.core.service.IConfigurationService;
import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.persistence.EntityManagerFactory;

/**
 * Index Persistence Information:
 * <p>
 * LUCENE BACKEND:
 * - Index is permanently stored on disk (default: ./lucene-indexes/)
 * - Survives application restarts
 * - Automatic updates during runtime for entity changes
 * - Only deleted by explicit dropAndCreate() or schema strategy
 * <p>
 * ELASTICSEARCH BACKEND:
 * - Index is stored in Elasticsearch cluster
 * - Persistent independent of Java application
 * <p>
 * AUTOMATIC UPDATES:
 * - Hibernate Search automatically updates the index on:
 *   - Entity INSERT/UPDATE/DELETE operations
 *   - Transaction commits
 *   - No manual reindexing needed for runtime changes
 */
@Singleton
public class ConfigurationService implements IConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);
    private static final IndexSummary INDEX_SUMMARY_NONE = new IndexSummary(0, 0, 0);

    private final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);
    private final IStatisticsService statisticsService = Jdp.getRequired(IStatisticsService.class);

    private record IndexSummary(int totalEntities, int indexedEntities, int nonIndexedEntities) { }
    private record IndexStatus(IndexSummary summary, String errorMessage) { }

    @Override
    public <T> void createIndexesFromScratch(@Nonnull RequestContext ctx, @Nonnull final Class<T> entityClass) throws InterruptedException {
        dropIndex(entityClass);
        createMissingIndexes(entityClass);
        final IndexStatus indexStatus = checkIndexStatusSub(entityClass);

        final StatisticsDTO stats = new StatisticsDTO();
        stats.setJobRef(ctx.internalHeaderParameters.getProcessRef());
        stats.setProcessId("CreateHibernateSearchIndex");
        stats.setStartTime(ctx.executionStart);
        stats.setEndTime(Instant.now());
        stats.setRecordsProcessed(indexStatus.summary.totalEntities);
        stats.setRecordsError(0);
        stats.setCount1(indexStatus.summary.indexedEntities);
        stats.setCount2(indexStatus.summary.nonIndexedEntities);
        statisticsService.saveStatisticsData(stats);
    }

    @Override
    public <T> void updateIndexes(@Nonnull final Class<T> entityClass) throws InterruptedException {
        createMissingIndexes(entityClass);
        checkIndexStatusSub(entityClass);
    }

    @Override
    @Nullable
    public <T> String checkIndexStatus(@Nonnull final Class<T> entityClass) {
        // return null if no error message
        return checkIndexStatusSub(entityClass).errorMessage;
    }

    /**
     * Drop index (deletes all data!)
     */
    private <T> void dropIndex(@Nonnull final Class<T> entityClass)  {

        final SearchMapping searchMapping = Search.mapping(emf);

        // Drop old indexes if it exists
        searchMapping.scope(entityClass).schemaManager().dropIfExisting();

        LOGGER.info("Index dropped for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Creates missing indexes
     */
    private <T> void createMissingIndexes(@Nonnull final Class<T> entityClass) throws InterruptedException {

        final SearchMapping searchMapping = Search.mapping(emf);
        final SearchScope<T> searchScope = searchMapping.scope(entityClass);
        final SearchSchemaManager schemaManager = searchScope.schemaManager();
        // Schema-Management
        schemaManager.createOrUpdate();

        // Validate Schema
        schemaManager.validate();

        // Mass Indexing
        searchScope.massIndexer().startAndWait();

        LOGGER.info("Indexes created for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Checks the current index status
     */
     @Nonnull
    private <T> IndexStatus checkIndexStatusSub(@Nonnull final Class<T> entityClass) {
        try {
            // Validate schema - throws exception if index doesn't exist
            final SearchMapping searchMapping = Search.mapping(emf);
            searchMapping.scope(entityClass).schemaManager().validate();
            LOGGER.info("Indexes exists and are valid for entity: {}", entityClass.getSimpleName());

            final IndexSummary indexSummary = printIndexSummary();
            return new IndexStatus(indexSummary, null);

        } catch (final Exception e) {
            LOGGER.warn("Index validation failed for entity {}: {}", entityClass.getSimpleName(), e.getMessage());
            return new IndexStatus(INDEX_SUMMARY_NONE, e.getMessage());
        }
    }

    /**
     * Prints a summary of all indexed entities and their fields
     */
    private IndexSummary printIndexSummary() {
        LOGGER.info("=== Hibernate Search Index Summary ===");
        final EntityConfigCache entityConfigCache = ConfigurationLoader.getEntityConfigCache();
        final SearchMapping searchMapping = Search.mapping(emf);
        int totalEntities = 0;
        int indexedEntities = 0;
        int nonIndexedEntities = 0;
        try {
            // Get all entity classes from the EntityManagerFactory
            final Set<EntityType<?>> entityTypes = emf.getMetamodel().getEntities();
            totalEntities = entityTypes.size();
            for (final EntityType<?> entityType : entityTypes) {
                final Class<?> entityClass = entityType.getJavaType();

                // Check if the entity is indexed by trying to create a scope for it
                try {
                    searchMapping.scope(entityClass);
                    final List<String> indexedFields = entityConfigCache.getIndexedFields(entityClass.getName());
                    LOGGER.info("Indexed Entity: {}", entityClass.getSimpleName());
                    LOGGER.info("  Indexed Fields: {}", String.join(", ", indexedFields));
                    indexedEntities++;
                } catch (final Exception e) {
                    // Entity is not indexed, skip it
                    nonIndexedEntities++;
                    LOGGER.debug("Entity {} is not indexed: {}", entityClass.getSimpleName(), e.getMessage());
                }
            }
            LOGGER.info("Total indexed entities: {}", indexedEntities);
        } catch (final Exception e) {
            LOGGER.error("Failed to print indexed summary: {}", e.getMessage());
        }
        return new IndexSummary(totalEntities, indexedEntities, nonIndexedEntities);
    }

}
