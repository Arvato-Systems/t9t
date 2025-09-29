package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(LongFilter.my$PQON)
public class LongFilterToPredicate extends AbstractNumericFilterToPredicate<LongFilter, Long> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final LongFilter longFilter) {
        return convertToPredicate(factory, longFilter.getFieldName(), longFilter.getValueList(), longFilter.getEqualsValue(),
                longFilter.getLowerBound(), longFilter.getUpperBound());
    }
}
