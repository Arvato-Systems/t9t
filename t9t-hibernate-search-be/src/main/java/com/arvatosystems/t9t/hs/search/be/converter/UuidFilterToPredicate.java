package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.base.T9tUtil;
import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
@Named(UuidFilter.my$PQON)
public class UuidFilterToPredicate extends AbstractStringFilterToPredicate<UuidFilter> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final UuidFilter uuidFilter) {
        if (T9tUtil.isNotEmpty(uuidFilter.getValueList())) {
            final List<String> values = new ArrayList<>(uuidFilter.getValueList().size());
            for (final UUID uuid : uuidFilter.getValueList()) {
                values.add(uuid.toString());
            }
            return convertToPredicate(entityName, factory, uuidFilter.getFieldName(), values, null, null);
        }
        if (uuidFilter.getEqualsValue() != null) {
            return factory.match().field(uuidFilter.getFieldName()).matching(uuidFilter.getEqualsValue().toString()).toPredicate();
        }
        return factory.matchAll().toPredicate();
    }
}
