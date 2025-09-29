package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.FloatFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(FloatFilter.my$PQON)
public class FloatFilterToPredicate extends AbstractNumericFilterToPredicate<FloatFilter, Float> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final FloatFilter floatFilter) {
        return convertToPredicate(factory, floatFilter.getFieldName(), floatFilter.getValueList(), floatFilter.getEqualsValue(),
                floatFilter.getLowerBound(), floatFilter.getUpperBound());
    }
}
