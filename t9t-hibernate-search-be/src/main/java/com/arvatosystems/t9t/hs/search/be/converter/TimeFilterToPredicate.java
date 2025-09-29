package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.TimeFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.time.LocalTime;

@Singleton
@Named(TimeFilter.my$PQON)
public class TimeFilterToPredicate extends AbstractTemporalFilterToPredicate<TimeFilter, LocalTime> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final TimeFilter timeFilter) {
        return convertToPredicate(factory, timeFilter.getFieldName(), timeFilter.getValueList(), timeFilter.getEqualsValue(),
            timeFilter.getLowerBound(), timeFilter.getUpperBound());
    }
}
