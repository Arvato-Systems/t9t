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
package com.arvatosystems.t9t.hs.search.be;

import static com.arvatosystems.t9t.hs.search.be.HibernateSearchHelper.getBool;

import java.util.List;
import java.util.Objects;

import org.hibernate.search.engine.search.common.NonStaticMetamodelScope;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.TypedSearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.hs.T9tHibernateSearchException;
import com.arvatosystems.t9t.hs.configurate.be.core.impl.EntityConfigurer;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;

@Singleton
@Named(T9tConstants.TEXT_SEARCH_ID_HIBERNATE_SEARCH)
public class HibernateSearchEngine implements ITextSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateSearchEngine.class);

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    @Override
    @Nonnull
    public DocumentTypeNeeded getDocumentTypeNeeded() {
        return DocumentTypeNeeded.ENTITY_NAME;
    }

    @Override
    @Nonnull
    public List<Long> search(@Nonnull final RequestContext ctx, @Nonnull final SearchCriteria sc, @Nonnull final String documentName,
        @Nullable final String resultFieldName) {

        final EntityManager em = jpaContextProvider.get().getEntityManager();
        if (em == null) {
            throw new T9tException(T9tException.GENERAL_EXCEPTION, "EntityManager is not available");
        }
        try {
            final Class<?> entityClass = Class.forName(documentName);
            final SearchSession searchSession = Search.session(em);

            final List<?> result = query(searchSession, entityClass, sc);
            return result.stream()
                    .map(entity -> HibernateSearchHelper.extractId(entity, resultFieldName))
                    .filter(Objects::nonNull)
                    .toList();

        } catch (final ClassNotFoundException e) {
            LOGGER.error("Entity class not found for documentName: {}", documentName, e);
            throw new T9tException(T9tHibernateSearchException.DOCUMENT_ENTITY_CLASS_NOT_FOUND, "Entity class not found for document: " + documentName);
        }
    }

    @Nonnull
    private <T> List<T> query(@Nonnull final SearchSession searchSession, @Nonnull final Class<T> entityClass, @Nonnull final SearchCriteria sc) {

        SearchQueryOptionsStep<NonStaticMetamodelScope, ?, T, SearchLoadingOptionsStep, ?, ?> query = null;

        if (sc.getSearchFilter() != null) {
            LOGGER.debug("Specific search with SearchFilter on {}", entityClass.getName());
            final SearchScope<?> scope = searchSession.scope(entityClass);
            final SearchPredicate predicate = HibernateSearchHelper.getSearchPredicate(entityClass.getName(), scope.predicate(), sc.getSearchFilter());
            query = searchSession.search(entityClass)
                    .where(predicate);

        } else if (T9tUtil.isNotBlank(sc.getExpression())) {
            LOGGER.debug("Performing expression search '{}' on {}", sc.getExpression(), entityClass.getName());
            query = searchSession.search(entityClass)
                    .where(factory -> getBool(factory, entityClass.getName(), null, sc.getExpression()));
        }

        if (query == null) {
            // If no specific query was built, default to match all
            LOGGER.debug("No specific search criteria provided, performing match all query on {}", entityClass.getName());
            query = searchSession.search(entityClass)
                    .where(TypedSearchPredicateFactory::matchAll);
        }

        // Add sorting conditions if any
        addSortConditions(sc, query);

        // Execute the query and return results
        return getResult(sc, query);
    }

    private <T> void addSortConditions(@Nonnull final SearchCriteria sc,
        @Nonnull SearchQueryOptionsStep<NonStaticMetamodelScope, ?, T, SearchLoadingOptionsStep, ?, ?> query) {

        if (T9tUtil.isEmpty(sc.getSortColumns())) {
            return; // No sorting criteria provided
        }
        for (final SortColumn sortCriteria : sc.getSortColumns()) {
            if (T9tUtil.isNotBlank(sortCriteria.getFieldName())) {
                final String originalField = sortCriteria.getFieldName();
                final String sortField = EntityConfigurer.getCachedSortFields().get(originalField);
                final boolean descending = sortCriteria.getDescending();
                try {
                    query = query.sort(f -> descending ? f.field(sortField).desc() : f.field(sortField).asc());
                } catch (Exception e) {
                    LOGGER.debug("Sort field '{}' not usable ({}), falling back to '{}'", sortField, e.getMessage(), originalField);
                    query = query.sort(f -> descending ? f.field(originalField).desc() : f.field(originalField).asc());
                }
            }
        }
    }

    @Nonnull
    private <T> List<T> getResult(@Nonnull final SearchCriteria sc,
        @Nonnull final SearchQueryOptionsStep<NonStaticMetamodelScope, ?, T, SearchLoadingOptionsStep, ?, ?> query) {

        if (sc.getOffset() != 0 || sc.getLimit() != 0) {
            final int limit = sc.getLimit() != 0 ? sc.getLimit() : Integer.MAX_VALUE;
            final int offset = sc.getOffset() != 0 ? sc.getOffset() : 0;
            return query.fetchHits(offset, limit);
        }
        return query.fetchAllHits();
    }
}
