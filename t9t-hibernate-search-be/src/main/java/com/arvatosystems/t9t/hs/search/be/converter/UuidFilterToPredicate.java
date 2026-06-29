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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Nonnull;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.base.T9tUtil;

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
