package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(AsciiFilter.my$PQON)
public class AsciiFilterToPredicate extends AbstractStringFilterToPredicate<AsciiFilter> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final AsciiFilter asciiFilter) {
        return convertToPredicate(entityName, factory, asciiFilter.getFieldName(), asciiFilter.getValueList(), asciiFilter.getEqualsValue(),
            asciiFilter.getLikeValue());
    }
}
