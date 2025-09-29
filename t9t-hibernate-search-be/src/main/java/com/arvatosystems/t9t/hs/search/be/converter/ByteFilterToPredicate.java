package com.arvatosystems.t9t.hs.search.be.converter;

import de.jpaw.bonaparte.pojos.api.ByteFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(ByteFilter.my$PQON)
public class ByteFilterToPredicate extends AbstractNumericFilterToPredicate<ByteFilter, Byte> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final ByteFilter byteFilter) {
        return convertToPredicate(factory, byteFilter.getFieldName(), byteFilter.getValueList(), byteFilter.getEqualsValue(),
                byteFilter.getLowerBound(), byteFilter.getUpperBound());
    }
}
