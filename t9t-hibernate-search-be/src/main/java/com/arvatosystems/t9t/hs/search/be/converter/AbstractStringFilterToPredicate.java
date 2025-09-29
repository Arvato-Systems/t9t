package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.base.T9tUtil;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.List;

import static com.arvatosystems.t9t.hs.search.be.HibernateSearchHelper.getBool;

public abstract class AbstractStringFilterToPredicate<FILTER extends FieldFilter> implements ISearchFilterToPredicate<FILTER> {

    @Nonnull
    protected SearchPredicate convertToPredicate(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory,
        @Nonnull final String fieldName, @Nullable final List<String> valueList, @Nullable final String equalsValue, @Nullable final String likeValue) {

        if (T9tUtil.isNotEmpty(valueList)) {
            return factory.terms().field(fieldName).matchingAny(valueList).toPredicate();
        }

        if (equalsValue != null) {
            return getBool(factory, entityName, fieldName, equalsValue).toPredicate();
        }

        if (likeValue != null) {
            if (!likeValue.contains("%") && !likeValue.contains("_")) {
                return getBool(factory, entityName, fieldName, likeValue).toPredicate();
            }
            String wildcardValue = likeValue.replace("%", "*").replace("_", "?");
            return factory.wildcard().field(fieldName).matching(wildcardValue).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }
}
