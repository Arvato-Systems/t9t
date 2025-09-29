package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(UnicodeFilter.my$PQON)
public class UnicodeFilterToPredicate extends AbstractStringFilterToPredicate<UnicodeFilter> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final UnicodeFilter unicodeFilter) {
        return convertToPredicate(entityName, factory, unicodeFilter.getFieldName(), unicodeFilter.getValueList(), unicodeFilter.getEqualsValue(),
            unicodeFilter.getLikeValue());
    }
}
