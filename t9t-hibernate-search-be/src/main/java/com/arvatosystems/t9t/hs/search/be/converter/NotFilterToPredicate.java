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

import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.hs.search.be.HibernateSearchHelper;

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
