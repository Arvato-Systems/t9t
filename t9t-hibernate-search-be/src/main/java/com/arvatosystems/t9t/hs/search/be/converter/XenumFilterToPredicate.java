package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.search.XenumFilter;
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
@Named(XenumFilter.my$PQON)
public class XenumFilterToPredicate implements ISearchFilterToPredicate<XenumFilter> {

    private final IEnumResolver enumResolver = Jdp.getRequired(IEnumResolver.class);

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final XenumFilter xenumFilter) {
        final String fieldName = xenumFilter.getFieldName();

        if (T9tUtil.isNotEmpty(xenumFilter.getTokenList())) {
            return factory.terms().field(fieldName).matchingAny(xenumFilter.getTokenList()).toPredicate();
        }

        if (T9tUtil.isNotEmpty(xenumFilter.getNameList())) {
            final List<Object> tokens = new ArrayList<>(xenumFilter.getNameList().size());
            for (final String name : xenumFilter.getNameList()) {
                tokens.add(enumResolver.getTokenByPqonAndInstance(xenumFilter.getXenumPqon(), name));
            }
            return factory.terms().field(fieldName).matchingAny(tokens).toPredicate();
        }

        String token = xenumFilter.getEqualsToken();
        if (token == null && xenumFilter.getEqualsName() != null) {
            token = enumResolver.getTokenByXEnumPqonAndInstance(xenumFilter.getXenumPqon(), xenumFilter.getEqualsName());
        }

        if (token != null) {
            return factory.match().field(fieldName).matching(token).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }
}
