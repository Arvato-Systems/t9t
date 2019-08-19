/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.base.search.EnumsetFilter;
import com.arvatosystems.t9t.base.search.XenumFilter;
import com.arvatosystems.t9t.base.search.XenumsetFilter;
import com.arvatosystems.t9t.base.services.IEnumResolver;

import de.jpaw.bonaparte.jpa.api.JpaFilterImpl;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Singleton
@Specializes
public class GenericSearchFilter extends JpaFilterImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSearchFilter.class);

    protected final IEnumResolver enumResolver = Jdp.getRequired(IEnumResolver.class);

    protected void mustHaveExactlyOneOf(FieldFilter filter, Object ... arg) {
        int countNonNull = 0;
        for (Object o : arg)
            if (o != null)
                ++countNonNull;
        if (countNonNull == 0) {
            LOGGER.error("Underspecified filter: {}", filter);
            throw new T9tException(T9tException.UNDERSPECIFIED_FILTER_PARAMETERS, filter.getClass().getSimpleName() + ": " + filter.getFieldName());
        }
        if (countNonNull > 1) {
            LOGGER.error("Overspecified filter: {}", filter);
            throw new T9tException(T9tException.OVERSPECIFIED_FILTER_PARAMETERS, filter.getClass().getSimpleName() + ": " + filter.getFieldName());
        }
    }

    protected Predicate equalsOrNull(CriteriaBuilder cb, Path<?> path, Object token) {
        if (token == null) {
            // use nullfilter
            return cb.isNull(path);
        }
        return cb.equal(path, token);
    }

    protected Predicate inCriteriaWithPossibleNull(CriteriaBuilder cb, Path<?> path, List<String> names, Function<String, Object> resolver) {
        boolean hasNull = false;
        List<Object> tokens = new ArrayList<Object>(names.size());
        for (String name : names) {
            Object token = resolver.apply(name);
            if (token == null)
                hasNull = true;
            else
                tokens.add(token);
        }
        // misc. optimizations for the query....
        if (!hasNull)
            return path.in(tokens);
        if (tokens.isEmpty())
            return cb.isNull(path);
        if (tokens.size() == 1)
            return cb.or(cb.isNull(path), cb.equal(path, tokens.get(0)));
        return cb.or(cb.isNull(path), path.in(tokens));
    }

    @Override
    public Predicate applyFilter(CriteriaBuilder cb, Path<?> path, FieldFilter filter) {

        if (filter instanceof XenumFilter) {
            XenumFilter f = (XenumFilter)filter;
            mustHaveExactlyOneOf(filter, f.getEqualsName(), f.getNameList(), f.getEqualsToken(), f.getTokenList());
            LOGGER.trace("Xenum search filter {}", f);
            if (f.getEqualsName() != null) {
                return equalsOrNull(cb, path, enumResolver.getTokenByXEnumPqonAndInstance(f.getXenumPqon(), f.getEqualsName()));
            }
            if (f.getEqualsToken() != null) {
                return cb.equal(path, f.getEqualsToken());
            }
            if (f.getTokenList() != null) {
                return path.in(f.getTokenList());
            }
            // search for name list. Must resolve the list elements
            return inCriteriaWithPossibleNull(cb, path, f.getNameList(), s -> enumResolver.getTokenByXEnumPqonAndInstance(f.getXenumPqon(), s));
        } else if (filter instanceof EnumFilter) {
            EnumFilter f = (EnumFilter)filter;
            mustHaveExactlyOneOf(filter, f.getEqualsName(), f.getNameList(), f.getEqualsToken(), f.getTokenList(), f.getEqualsOrdinal(), f.getOrdinalList());
            LOGGER.trace("Enum search filter {}", f);

            if (f.getEqualsName() != null) {
                return equalsOrNull(cb, path, enumResolver.getTokenByPqonAndInstance(f.getEnumPqon(), f.getEqualsName()));
            }
            if (f.getEqualsToken() != null) {
                return cb.equal(path, f.getEqualsToken());
            }
            if (f.getTokenList() != null) {
                return path.in(f.getTokenList());
            }
            if (f.getEqualsOrdinal() != null) {
                return cb.equal(path, f.getEqualsOrdinal());
            }
            if (f.getOrdinalList() != null) {
                return path.in(f.getOrdinalList());
            }
            // search for name list. Must resolve the list elements
            return inCriteriaWithPossibleNull(cb, path, f.getNameList(), s -> enumResolver.getTokenByPqonAndInstance(f.getEnumPqon(), s));
        } else if (filter instanceof XenumsetFilter) {
            XenumsetFilter f = (XenumsetFilter)filter;
            mustHaveExactlyOneOf(filter, f.getEqualsName(), f.getEqualsToken());
            LOGGER.trace("Enumset search filter {}", f);
            Object token = f.getEqualsToken();
            if (f.getEqualsName() != null) {
                token = enumResolver.getTokenByXEnumSetPqonAndInstance(f.getXenumsetPqon(), f.getEqualsName());
            }
            if (f.getSubset())
                return cb.like((Expression<String>) path, "%" + token + "%");
            else
                return cb.equal(path, token);
        } else if (filter instanceof EnumsetFilter) {
            EnumsetFilter f = (EnumsetFilter)filter;
            mustHaveExactlyOneOf(filter, f.getEqualsName(), f.getEqualsToken());
            LOGGER.trace("Enumset search filter {}", f);
            Object token = f.getEqualsToken();
            if (f.getEqualsName() != null) {
                token = enumResolver.getTokenBySetPqonAndInstance(f.getEnumsetPqon(), f.getEqualsName());
            }
            if (token == null) {
                // use nullfilter
                return cb.isNull(path);
            }
            // token or ordinal
            if (f.getSubset() && token instanceof String)
                return cb.like((Expression<String>) path, "%" + (String)token + "%");
            else
                return cb.equal(path, token);  // numeric: not subset but exact value (token is really an ordinal)
        } else {
            return super.applyFilter(cb, path, filter);
        }
    }
}
