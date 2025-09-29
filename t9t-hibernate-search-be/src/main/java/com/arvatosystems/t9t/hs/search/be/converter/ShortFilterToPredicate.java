package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.ShortFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(ShortFilter.my$PQON)
public class ShortFilterToPredicate extends AbstractNumericFilterToPredicate<ShortFilter, Short> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final ShortFilter shortFilter) {
        return convertToPredicate(factory, shortFilter.getFieldName(), shortFilter.getValueList(), shortFilter.getEqualsValue(),
                shortFilter.getLowerBound(), shortFilter.getUpperBound());
    }
}
