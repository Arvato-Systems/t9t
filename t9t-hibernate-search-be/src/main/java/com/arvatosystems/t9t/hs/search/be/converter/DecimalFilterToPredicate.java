package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.DecimalFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.math.BigDecimal;

@Singleton
@Named(DecimalFilter.my$PQON)
public class DecimalFilterToPredicate extends AbstractNumericFilterToPredicate<DecimalFilter, BigDecimal> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory,
        @Nonnull final DecimalFilter decimalFilter) {
        return convertToPredicate(factory, decimalFilter.getFieldName(), decimalFilter.getValueList(), decimalFilter.getEqualsValue(),
            decimalFilter.getLowerBound(), decimalFilter.getUpperBound());
    }
}
