package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(BooleanFilter.my$PQON)
public class BooleanFilterToPredicate implements ISearchFilterToPredicate<BooleanFilter> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory,
        @Nonnull final BooleanFilter booleanFilter) {
        return factory.match().field(booleanFilter.getFieldName()).matching(booleanFilter.getBooleanValue()).toPredicate();
    }
}
