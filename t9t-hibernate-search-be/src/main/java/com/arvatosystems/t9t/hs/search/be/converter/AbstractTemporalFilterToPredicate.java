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

import java.time.temporal.Temporal;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import de.jpaw.bonaparte.pojos.api.FieldFilter;

import com.arvatosystems.t9t.base.T9tUtil;

public abstract class AbstractTemporalFilterToPredicate<FILTER extends FieldFilter, FIELD_TYPE extends Temporal> implements ISearchFilterToPredicate<FILTER> {

    @Nonnull
    protected SearchPredicate convertToPredicate(@Nonnull final SearchPredicateFactory factory,
        @Nonnull final String fieldName, @Nullable final List<FIELD_TYPE> valueList, @Nullable final FIELD_TYPE equalsValue,
        @Nullable final FIELD_TYPE lowerBound, @Nullable final FIELD_TYPE upperBound) {

        if (equalsValue != null) {
            return factory.match().field(fieldName).matching(equalsValue).toPredicate();
        }

        if (T9tUtil.isNotEmpty(valueList)) {
            return factory.terms().field(fieldName).matchingAny(valueList).toPredicate();
        }

        if (lowerBound != null && upperBound != null) {
            return factory.range().field(fieldName).between(lowerBound, upperBound).toPredicate();

        } else if (lowerBound != null) {
            return factory.range().field(fieldName).atLeast(lowerBound).toPredicate();

        } else if (upperBound != null) {
            return factory.range().field(fieldName).atMost(upperBound).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }
}
