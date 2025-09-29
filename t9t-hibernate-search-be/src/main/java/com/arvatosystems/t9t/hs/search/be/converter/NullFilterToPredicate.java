package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(NullFilter.my$PQON)
public class NullFilterToPredicate implements ISearchFilterToPredicate<NullFilter> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final NullFilter nullFilter) {
        // NULL means field does not exist or has no value
        return factory.bool().mustNot(factory.exists().field(nullFilter.getFieldName())).toPredicate();
    }
}
