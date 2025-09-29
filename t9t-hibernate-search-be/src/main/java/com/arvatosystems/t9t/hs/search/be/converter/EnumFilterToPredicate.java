package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.base.services.IEnumResolver;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
@Named(EnumFilter.my$PQON)
public class EnumFilterToPredicate implements ISearchFilterToPredicate<EnumFilter> {

    private final IEnumResolver enumResolver = Jdp.getRequired(IEnumResolver.class);

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final EnumFilter enumFilter) {
        final String fieldName = enumFilter.getFieldName();

        if (T9tUtil.isNotEmpty(enumFilter.getTokenList())) {
            return factory.terms().field(fieldName).matchingAny(enumFilter.getTokenList()).toPredicate();
        }

        if (T9tUtil.isNotEmpty(enumFilter.getNameList())) {
            final List<Object> tokens = new ArrayList<>(enumFilter.getNameList().size());
            for (final String name : enumFilter.getNameList()) {
                tokens.add(enumResolver.getTokenByPqonAndInstance(enumFilter.getEnumPqon(), name));
            }
            return factory.terms().field(fieldName).matchingAny(tokens).toPredicate();
        }

        Object token = enumFilter.getEqualsToken();
        if (token == null && enumFilter.getEqualsName() != null) {
            token = enumResolver.getTokenByPqonAndInstance(enumFilter.getEnumPqon(), enumFilter.getEqualsName());
        }

        if (token != null) {
            return factory.match().field(fieldName).matching(token).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }
}
