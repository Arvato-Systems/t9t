package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.DayFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.time.LocalDate;

@Singleton
@Named(DayFilter.my$PQON)
public class DayFilterToPredicate extends AbstractTemporalFilterToPredicate<DayFilter, LocalDate> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final DayFilter dayFilter) {
        return convertToPredicate(factory, dayFilter.getFieldName(), dayFilter.getValueList(), dayFilter.getEqualsValue(),
            dayFilter.getLowerBound(), dayFilter.getUpperBound());
    }
}
