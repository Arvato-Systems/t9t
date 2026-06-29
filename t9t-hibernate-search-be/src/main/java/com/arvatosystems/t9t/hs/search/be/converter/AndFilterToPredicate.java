/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.hs.search.be.converter;

import jakarta.annotation.Nonnull;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.hs.search.be.HibernateSearchHelper;

@Singleton
@Named(AndFilter.my$PQON)
public class AndFilterToPredicate implements ISearchFilterToPredicate<AndFilter> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndFilterToPredicate.class);

    @Nonnull
    @Override
    public SearchPredicate convert(@Nonnull final String entityName, @Nonnull final SearchPredicateFactory factory, @Nonnull final AndFilter andFilter) {
        final SearchFilter filter1 = andFilter.getFilter1();
        final SearchFilter filter2 = andFilter.getFilter2();
        if (filter1 == null || filter2 == null) {
            LOGGER.error("Filter can't be null in AND filter: {}", andFilter);
            throw new T9tException(T9tException.SEARCH_FILTER_VALIDATION_ERROR);
        }
        final SearchPredicate predicate1 = HibernateSearchHelper.getSearchPredicate(entityName, factory, filter1);
        final SearchPredicate predicate2 = HibernateSearchHelper.getSearchPredicate(entityName, factory, filter2);
        return factory.and(predicate1, predicate2).toPredicate();
    }
}
