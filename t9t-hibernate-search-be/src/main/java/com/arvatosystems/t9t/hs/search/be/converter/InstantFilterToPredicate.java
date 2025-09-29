package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.time.Instant;

@Singleton
@Named(InstantFilter.my$PQON)
public class InstantFilterToPredicate extends AbstractTemporalFilterToPredicate<InstantFilter, Instant> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory,
        @Nonnull final InstantFilter instantFilter) {
        return convertToPredicate(factory, instantFilter.getFieldName(), instantFilter.getValueList(), instantFilter.getEqualsValue(),
            instantFilter.getLowerBound(), instantFilter.getUpperBound());
    }
}
