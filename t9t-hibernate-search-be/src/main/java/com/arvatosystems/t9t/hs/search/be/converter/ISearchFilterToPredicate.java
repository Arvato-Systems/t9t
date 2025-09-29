package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

public interface ISearchFilterToPredicate<FILTER extends SearchFilter> {

    /**
     * Converts the given search filter into a Hibernate Search predicate.
     *
     * @param entityName the name of the entity being searched
     * @param factory    the {@link SearchPredicateFactory} to create predicates
     * @param searchFilter the {@link SearchFilter} to convert
     * @return the resulting {@link SearchPredicateFactory}
     */
    @Nonnull
    SearchPredicate convert(@Nonnull String entityName, @Nonnull SearchPredicateFactory factory, @Nonnull FILTER searchFilter);
}
