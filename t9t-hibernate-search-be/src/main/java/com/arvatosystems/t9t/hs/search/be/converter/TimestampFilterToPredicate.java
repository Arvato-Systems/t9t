package com.arvatosystems.t9t.hs.search.be.converter;

import java.time.LocalDateTime;

import jakarta.annotation.Nonnull;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import de.jpaw.bonaparte.pojos.api.TimestampFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named(TimestampFilter.my$PQON)
public class TimestampFilterToPredicate extends AbstractTemporalFilterToPredicate<TimestampFilter, LocalDateTime> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory,
        @Nonnull final TimestampFilter timestampFilter) {
        return convertToPredicate(factory, timestampFilter.getFieldName(), timestampFilter.getValueList(), timestampFilter.getEqualsValue(),
            timestampFilter.getLowerBound(), timestampFilter.getUpperBound());
    }
}
