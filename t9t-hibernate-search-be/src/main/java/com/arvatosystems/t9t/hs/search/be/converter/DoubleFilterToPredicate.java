package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.DoubleFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(DoubleFilter.my$PQON)
public class DoubleFilterToPredicate extends AbstractNumericFilterToPredicate<DoubleFilter, Double> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final DoubleFilter doubleFilter) {
        return convertToPredicate(factory, doubleFilter.getFieldName(), doubleFilter.getValueList(), doubleFilter.getEqualsValue(),
                doubleFilter.getLowerBound(), doubleFilter.getUpperBound());
    }
}
