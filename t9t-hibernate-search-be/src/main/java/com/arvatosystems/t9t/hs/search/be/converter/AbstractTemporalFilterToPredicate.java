package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.base.T9tUtil;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.time.temporal.Temporal;
import java.util.List;

public abstract class AbstractTemporalFilterToPredicate<FILTER extends FieldFilter, FIELD_TYPE extends Temporal> implements ISearchFilterToPredicate<FILTER> {

    @Nonnull
    protected SearchPredicate convertToPredicate(@Nonnull final SearchPredicateFactory factory,
        @Nonnull final String fieldName, @Nullable final List<FIELD_TYPE> valueList, @Nullable final FIELD_TYPE equalsValue,
        @Nullable final FIELD_TYPE lowerBound, @Nullable final FIELD_TYPE upperBound) {

        if (equalsValue != null) {
            return factory.match().field(fieldName).matching(equalsValue).toPredicate();
        }

        if (T9tUtil.isNotEmpty(valueList)) {
            return factory.terms().field(fieldName).matchingAny(valueList).toPredicate();
        }

        if (lowerBound != null && upperBound != null) {
            return factory.range().field(fieldName).between(lowerBound, upperBound).toPredicate();

        } else if (lowerBound != null) {
            return factory.range().field(fieldName).atLeast(lowerBound).toPredicate();

        } else if (upperBound != null) {
            return factory.range().field(fieldName).atMost(upperBound).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }
}
