package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(IntFilter.my$PQON)
public class IntFilterToPredicate extends AbstractNumericFilterToPredicate<IntFilter, Integer> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final IntFilter intFilter) {
        return convertToPredicate(factory, intFilter.getFieldName(), intFilter.getValueList(), intFilter.getEqualsValue(),
                intFilter.getLowerBound(), intFilter.getUpperBound());
    }
}
