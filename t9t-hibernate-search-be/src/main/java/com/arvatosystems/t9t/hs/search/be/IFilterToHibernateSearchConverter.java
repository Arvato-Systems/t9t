package com.arvatosystems.t9t.hs.search.be;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

public interface IFilterToHibernateSearchConverter {

    SearchPredicate convertSearchFilterToPredicate(SearchPredicateFactory factory, SearchFilter searchFilter);
}
