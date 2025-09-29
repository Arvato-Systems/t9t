package com.arvatosystems.t9t.hs.search.be.converter;

import com.arvatosystems.t9t.hs.search.be.HibernateSearchHelper;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

@Singleton
@Named(NotFilter.my$PQON)
public class NotFilterToPredicate implements ISearchFilterToPredicate<NotFilter> {

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final NotFilter notFilter) {
        if (notFilter.getFilter() instanceof NullFilter f) {
            // Special case: NOT NULL
            return factory.exists().field(f.getFieldName()).toPredicate();
        }

        if (notFilter.getFilter() instanceof NotFilter f) {
            // Double negation: just return the inner filter
            return HibernateSearchHelper.getSearchPredicate(entityName, factory, f.getFilter());
        }

        return factory.bool().mustNot(HibernateSearchHelper.getSearchPredicate(entityName, factory, notFilter.getFilter())).toPredicate();
    }
}
