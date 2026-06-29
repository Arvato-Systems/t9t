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

import de.jpaw.bonaparte.pojos.api.SearchFilter;

public interface ISearchFilterToPredicate<FILTER extends SearchFilter> {

    /**
     * Converts the given search filter into a Hibernate Search predicate.
     *
     * @param entityName the name of the entity being searched
     * @param factory    the {@link SearchPredicateFactory} to create predicates
     * @param searchFilter the {@link SearchFilter} to convert
     * @return the resulting {@link SearchPredicateFactory}
     */
    @Nonnull
    SearchPredicate convert(@Nonnull String entityName, @Nonnull SearchPredicateFactory factory, @Nonnull FILTER searchFilter);
}
