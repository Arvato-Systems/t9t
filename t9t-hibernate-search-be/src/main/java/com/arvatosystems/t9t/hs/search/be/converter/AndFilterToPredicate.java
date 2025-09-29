package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.hs.search.be.HibernateSearchHelper;
import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named(AndFilter.my$PQON)
public class AndFilterToPredicate implements ISearchFilterToPredicate<AndFilter> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndFilterToPredicate.class);

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final AndFilter andFilter) {
        final SearchFilter filter1 = andFilter.getFilter1();
        final SearchFilter filter2 = andFilter.getFilter2();
        if (filter1 == null || filter2 == null) {
            LOGGER.error("Filter can't be null in AND filter: {}", andFilter);
            throw new T9tException(T9tException.SEARCH_FILTER_VALIDATION_ERROR);
        }
        final SearchPredicate predicate1 = HibernateSearchHelper.getSearchPredicate(entityName, factory, filter1);
        final SearchPredicate predicate2 = HibernateSearchHelper.getSearchPredicate(entityName, factory, filter2);
        return factory.and(predicate1, predicate2).toPredicate();
    }
}
