package com.arvatosystems.t9t.hs.search.be.impl;

import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import jakarta.persistence.EntityManager;
import org.hibernate.search.engine.search.common.NonStaticMetamodelScope;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.TypedSearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.util.common.SearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Singleton
@Named("HIBERNATE-SEARCH")
public class HibernateSearchEngine implements ITextSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateSearchEngine.class);

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    @Override
    public List<Long> search(RequestContext ctx, SearchCriteria sc, String documentName, String resultFieldName) {

        final EntityManager em = jpaContextProvider.get().getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager is not available");
        }
        try {
            Class<?> entityClass = Class.forName(documentName);
            SearchSession searchSession = Search.session(em);

            List<?> result = query(searchSession, entityClass, sc);
            return result.stream()
                    .map(entity -> HibernateSearchHelper.extractId(entity, resultFieldName))
                    .filter(Objects::nonNull)
                    .toList();

        } catch (ClassNotFoundException e) {
            LOGGER.error("Entity class not found for documentName: {}", documentName, e);
            throw new IllegalArgumentException("Unknown entity: " + documentName);
        } catch (SearchException e) {
            LOGGER.warn("Entity {} search exception", documentName);
            throw new IllegalArgumentException("Entity not indexed: " + documentName, e);
        } catch (Exception e) {
            LOGGER.error("HibernateEngine: Search error", e);
            throw new RuntimeException(e);
        }
    }

    private static <T> List<T> query(SearchSession searchSession, Class<T> entityClass, SearchCriteria sc) {

        SearchQueryOptionsStep<NonStaticMetamodelScope, ?, T, SearchLoadingOptionsStep, ?, ?> query = null;

        if (sc.getSearchFilter() != null) {
            LOGGER.debug("Specific search with SearchFilter on {}", entityClass.getName());
            SearchScope<?> scope = searchSession.scope(entityClass);
            FilterToHibernateSearchConverter converter = new FilterToHibernateSearchConverter();
            SearchPredicate predicate = converter.convertSearchFilterToPredicate(scope.predicate(), sc.getSearchFilter());
            query = searchSession.search(entityClass)
                    .where(predicate);

        } else if (sc.getExpression() != null && !sc.getExpression().isEmpty()) {
            /**
             * "John"              // simple search
             * "John + Doe"        // AND
             * "John | Jane"       // OR
             * "John -Doe"         // Exclude Doe
             * "John*"             // Wildcard (starting with John)
             * "\"John Doe\""      // Exact phrase
             * "+John -Doe"        // has to contain John, but not Doe
             */
            String[] fields = sc.getExpression().split(":", 2);
            if (fields.length == 2) {
                LOGGER.debug("Performing search with expression '{}' on {}", sc.getExpression(), entityClass.getName());
                query = searchSession.search(entityClass)
                        .where(f -> f.simpleQueryString().fields(fields[0].trim()).matching(fields[1].trim()));
            }
        }

        if (query == null) {
            // If no specific query was built, default to match all
            LOGGER.debug("No specific search criteria provided, performing match all query on {}", entityClass.getName());
            query = searchSession.search(entityClass)
                    .where(TypedSearchPredicateFactory::matchAll);
        }

        // Add sorting conditions if any
        query = addSortConditions(sc, query);

        // Execute the query and return results
        return getResult(sc, query);
    }

    private static <T> SearchQueryOptionsStep<NonStaticMetamodelScope, ?, T, SearchLoadingOptionsStep, ?, ?> addSortConditions(SearchCriteria sc, SearchQueryOptionsStep<NonStaticMetamodelScope, ?, T, SearchLoadingOptionsStep, ?, ?> query) {

        if (sc.getSortColumns() == null || sc.getSortColumns().isEmpty()) {
            return query; // No sorting criteria provided
        }
        for (SortColumn sortCriteria : sc.getSortColumns()) {
            if (sortCriteria.getFieldName() != null) {
                query = query.sort(f -> {
                    if (sortCriteria.getDescending()) {
                        return f.field(sortCriteria.getFieldName()).desc();
                    } else {
                        return f.field(sortCriteria.getFieldName()).asc();
                    }
                });
            }
        }
        return query;
    }

    private static <T> List<T> getResult(SearchCriteria sc, SearchQueryOptionsStep<NonStaticMetamodelScope, ?, T, SearchLoadingOptionsStep, ?, ?> query) {

        if (sc.getOffset() != 0 || sc.getLimit() != 0) {
            int limit = sc.getLimit() != 0 ? sc.getLimit() : Integer.MAX_VALUE;
            int offset = sc.getOffset() != 0 ? sc.getOffset() : 0;
            return query.fetchHits(offset, limit);
        }
        return query.fetchAllHits();
    }
}
