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
package com.arvatosystems.t9t.hs.search.be.impl;

import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.base.search.XenumFilter;
import com.arvatosystems.t9t.base.services.IEnumResolver;
import com.arvatosystems.t9t.hs.search.be.IFilterToHibernateSearchConverter;
import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.bonaparte.pojos.api.DayFilter;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.TimeFilter;
import de.jpaw.bonaparte.pojos.api.TimestampFilter;
import de.jpaw.bonaparte.pojos.api.DecimalFilter;
import de.jpaw.bonaparte.pojos.api.DoubleFilter;
import de.jpaw.bonaparte.pojos.api.FloatFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.List;

import static com.arvatosystems.t9t.hs.search.be.impl.HibernateSearchHelper.getBool;

@Singleton
public class FilterToHibernateSearchConverter implements IFilterToHibernateSearchConverter {

    private final IEnumResolver enumResolver = Jdp.getRequired(IEnumResolver.class);

    @Override
    public SearchPredicate convertSearchFilterToPredicate(String entityName, final SearchPredicateFactory factory, final SearchFilter searchFilter) {

        if (searchFilter == null) {
            return factory.matchAll().toPredicate();
        }

        if (searchFilter instanceof AndFilter andFilter) {
            return convertAndToPredicate(entityName, factory, andFilter);

        } else if (searchFilter instanceof OrFilter orFilter) {
            return convertOrToPredicate(entityName, factory, orFilter);

        } else if (searchFilter instanceof NullFilter nullFilter) {
            return convertNullToPredicate(factory, nullFilter);

        } else if (searchFilter instanceof NotFilter notFilter) {
            return convertNotFilterToPredicate(entityName, factory, notFilter);

        } else if (searchFilter instanceof FieldFilter fieldFilter) {
            return convertFieldFilterToPredicate(entityName, factory, fieldFilter);

        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + searchFilter.getClass().getSimpleName());
        }
    }

    private SearchPredicate convertAndToPredicate(String entityName, final SearchPredicateFactory factory, final AndFilter filter) {

        return factory.and(
                convertSearchFilterToPredicate(entityName, factory, filter.getFilter1()),
                convertSearchFilterToPredicate(entityName, factory, filter.getFilter2())
        ).toPredicate();
    }

    private SearchPredicate convertOrToPredicate(String entityName, final SearchPredicateFactory factory, final OrFilter filter) {

        return factory.or(
                convertSearchFilterToPredicate(entityName, factory, filter.getFilter1()),
                convertSearchFilterToPredicate(entityName, factory, filter.getFilter2())
        ).toPredicate();
    }

    private SearchPredicate convertNullToPredicate(final SearchPredicateFactory factory, final NullFilter filter) {

        // NULL means field does not exist or has no value
        return factory.bool().mustNot(factory.exists().field(filter.getFieldName())).toPredicate();
    }

    private SearchPredicate convertNotFilterToPredicate(String entityName, final SearchPredicateFactory factory, final NotFilter filter) {

        if (filter.getFilter() instanceof NullFilter f) {
            // Special case: NOT NULL
            return factory.exists().field(f.getFieldName()).toPredicate();
        }

        if (filter.getFilter() instanceof NotFilter f) {
            // Double negation: just return the inner filter
            return convertSearchFilterToPredicate(entityName, factory, f.getFilter());
        }

        return factory.bool().mustNot(convertSearchFilterToPredicate(entityName, factory, filter.getFilter())).toPredicate();
    }

    private SearchPredicate convertFieldFilterToPredicate(String entityName, final SearchPredicateFactory factory, final FieldFilter filter) {

        if (filter instanceof AsciiFilter f) {
            return convertStringToPredicate(entityName, factory, f.getFieldName(), f.getValueList(), f.getEqualsValue(), f.getLikeValue());

        } else if (filter instanceof UnicodeFilter f) {
            return convertStringToPredicate(entityName, factory, f.getFieldName(), f.getValueList(), f.getEqualsValue(), f.getLikeValue());

        } else if (filter instanceof BooleanFilter bf) {
            return convertBooleanToPredicate(factory, bf);

        } else if (filter instanceof UuidFilter uuf) {
            return convertUUidToPredicate(factory, uuf);

        } else if (filter instanceof EnumFilter ef) {
            return convertEnumToPredicate(factory, ef);

        } else if (filter instanceof XenumFilter xf) {
            return convertXenumToPredicate(factory, xf);

        } else if (filter instanceof DayFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof InstantFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof TimeFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof TimestampFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof DecimalFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof DoubleFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof FloatFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof IntFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else if (filter instanceof LongFilter f) {
            return convertNumericOrTemporalToPredicate(factory, f.getFieldName(), f.getEqualsValue(), f.getValueList(), f.getLowerBound(), f.getUpperBound());

        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + filter.getClass().getSimpleName());
        }
    }

    private SearchPredicate convertStringToPredicate(String entityName,
                                                     final SearchPredicateFactory factory,
                                                     String fieldName,
                                                     List<String> valueList,
                                                     String equalsValue,
                                                     String likeValue) {
        if (valueList != null && !valueList.isEmpty()) {
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

    private SearchPredicate convertNumericOrTemporalToPredicate(final SearchPredicateFactory factory,
                                                                String fieldName,
                                                                Object equalsValue,
                                                                List<?> valueList,
                                                                Object lowerBound,
                                                                Object upperBound) {
        if (equalsValue != null) {
            return factory.match().field(fieldName).matching(equalsValue).toPredicate();
        }

        if (valueList != null && !valueList.isEmpty()) {
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

    private SearchPredicate convertBooleanToPredicate(SearchPredicateFactory factory, BooleanFilter filter) {

        String fieldName = filter.getFieldName();

        if (Boolean.TRUE.equals(filter.getBooleanValue())) {
            return factory.match().field(fieldName).matching(true).toPredicate();
        }

        if (Boolean.FALSE.equals(filter.getBooleanValue())) {
            return factory.match().field(fieldName).matching(false).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }

    private SearchPredicate convertUUidToPredicate(final SearchPredicateFactory factory, final UuidFilter uuidFilter) {

        String fieldName = uuidFilter.getFieldName();

        if (uuidFilter.getValueList() != null && !uuidFilter.getValueList().isEmpty()) {
            List<String> stringValues = uuidFilter.getValueList().stream()
                    .map(Object::toString)
                    .toList();
            return factory.terms().field(fieldName).matchingAny(stringValues).toPredicate();
        }

        if (uuidFilter.getEqualsValue() != null) {
            return factory.match().field(fieldName).matching(uuidFilter.getEqualsValue().toString()).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }

    private SearchPredicate convertEnumToPredicate(final SearchPredicateFactory factory, final EnumFilter filter) {

        String fieldName = filter.getFieldName();

        if (filter.getTokenList() != null && !filter.getTokenList().isEmpty()) {
            return factory.terms().field(fieldName).matchingAny(filter.getTokenList()).toPredicate();
        }

        if (filter.getNameList() != null && !filter.getNameList().isEmpty()) {
            List<Object> tokens = filter.getNameList().stream()
                    .map(name -> enumResolver.getTokenByPqonAndInstance(filter.getEnumPqon(), name))
                    .toList();
            return factory.terms().field(fieldName).matchingAny(tokens).toPredicate();
        }

        Object token = filter.getEqualsToken();
        if (token == null && filter.getEqualsName() != null) {
            token = enumResolver.getTokenByPqonAndInstance(filter.getEnumPqon(), filter.getEqualsName());
        }

        if (token != null) {
            return factory.match().field(fieldName).matching(token).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }

    private SearchPredicate convertXenumToPredicate(final SearchPredicateFactory factory, final XenumFilter filter) {
        String fieldName = filter.getFieldName();

        if (filter.getTokenList() != null && !filter.getTokenList().isEmpty()) {
            return factory.terms().field(fieldName).matchingAny(filter.getTokenList()).toPredicate();
        }

        if (filter.getNameList() != null && !filter.getNameList().isEmpty()) {
            List<String> tokens = filter.getNameList().stream()
                    .map(name -> enumResolver.getTokenByXEnumPqonAndInstance(filter.getXenumPqon(), name))
                    .toList();
            return factory.terms().field(fieldName).matchingAny(tokens).toPredicate();
        }

        String token = filter.getEqualsToken();
        if (token == null && filter.getEqualsName() != null) {
            token = enumResolver.getTokenByXEnumPqonAndInstance(filter.getXenumPqon(), filter.getEqualsName());
        }

        if (token != null) {
            return factory.match().field(fieldName).matching(token).toPredicate();
        }

        return factory.matchAll().toPredicate();
    }
}
