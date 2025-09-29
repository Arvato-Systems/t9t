package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.hs.search.be.HibernateSearchHelper;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named(OrFilter.my$PQON)
public class OrFilterToPredicate implements ISearchFilterToPredicate<OrFilter> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrFilterToPredicate.class);

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final OrFilter orFilter) {
        final SearchFilter filter1 = orFilter.getFilter1();
        final SearchFilter filter2 = orFilter.getFilter2();
        if (filter1 == null || filter2 == null) {
            LOGGER.error("Filter can't be null in OR filter: {}", orFilter);
            throw new T9tException(T9tException.SEARCH_FILTER_VALIDATION_ERROR);
        }
        final SearchPredicate predicate1 = HibernateSearchHelper.getSearchPredicate(entityName, factory, filter1);
        final SearchPredicate predicate2 = HibernateSearchHelper.getSearchPredicate(entityName, factory, filter2);
        return factory.or(predicate1, predicate2).toPredicate();
    }
}
