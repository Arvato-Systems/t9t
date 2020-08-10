/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.services.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.IRestrictionRefReader;
import com.arvatosystems.t9t.base.services.ISearchRestriction;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;

public abstract class AbstractSearchRestriction implements ISearchRestriction {
    private final Cache<Long, List<Long>> allowedRefsCache = CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).<Long, List<Long>>build();

    protected static List<Long> NO_REFS = ImmutableList.<Long>of();
    protected static List<String> NO_FIELDS = ImmutableList.<String>of();

    @Override
    public void addRestrictionsForFields(final RequestContext ctx, final SearchCriteria srq, final List<String> pathnames) {
        this.addRestrictionsForFields(ctx, srq, pathnames, AbstractSearchRestriction.NO_FIELDS);
    }

    @Override
    public void addRestrictionsForFields(final RequestContext ctx, final SearchCriteria srq, final List<String> requiredFields, final List<String> optionalFields) {
        final List<Long> allowedRefs = this.retrieveAllowedRefsCached(ctx);
        if (allowedRefs == null || allowedRefs.isEmpty()) {
            return;
        }
        if (allowedRefs.size() == 1) {
            final Long singleRef = allowedRefs.get(0);
            for (final String name : requiredFields) {
                final LongFilter longFilter = new LongFilter(name);
                longFilter.setEqualsValue(singleRef);
                srq.setSearchFilter(SearchFilters.and(srq.getSearchFilter(), longFilter));
            }
            for (final String name_1 : optionalFields) {
                final LongFilter longFilter = new LongFilter(name_1);
                longFilter.setEqualsValue(singleRef);
                srq.setSearchFilter(SearchFilters.and(srq.getSearchFilter(), SearchFilters.or(new NullFilter(name_1), longFilter)));
            }
        } else {
            for (final String name_2 : requiredFields) {
                final LongFilter longFilter = new LongFilter(name_2);
                longFilter.setValueList(allowedRefs);
                srq.setSearchFilter(SearchFilters.and(srq.getSearchFilter(), longFilter));
            }
            for (final String name_3 : optionalFields) {
                final LongFilter longFilter = new LongFilter(name_3);
                longFilter.setValueList(allowedRefs);
                srq.setSearchFilter(SearchFilters.and(srq.getSearchFilter(), SearchFilters.or(new NullFilter(name_3), longFilter)));
            }
        }
    }

    /**
     * Returns the passed object as an immutable list of a single entry of type Long.
     * Due to encoding in JSON, the passed value could be returned as int or double, this is why we may need some conversion.
     */
    protected List<Long> asSingletonListOfLong(Object value) {
        if (value instanceof Long) {
            return ImmutableList.<Long>of(((Long) value));
        } else if (value instanceof Number) {
            // we cannot use it as is, but we can convert it
            return ImmutableList.<Long>of((((Number) value).longValue()));
        } else {
            throw new T9tException(T9tException.INVALID_REQUEST_PARAMETER_TYPE, "Required a Long or Number, but got " + value.getClass().getCanonicalName());
        }
    }

    /** Optional common method to check for JWT information and to read from persistence layer otherwise.
     * It is expected that the lampda parameter is provided as a reference to an instance of an implementation
     * of the functional interface IRestrictionRefReader, this is why that is provided within this package.
     */
    protected List<Long> retrieveAllowedRefsUncached(final RequestContext ctx, IRestrictionRefReader reader, String idKey, String refKey) {
        final Map<String, Object> z = ctx.internalHeaderParameters.getJwtInfo().getZ();
        if (z != null) {
            final Object id = z.get(idKey);
            if ("*".equals(id)) {
                return AbstractSearchRestriction.NO_REFS;
            }
            final Object ref = z.get(refKey);
            if (ref != null) {
                return asSingletonListOfLong(ref);
            }
        }
        return reader.objectRefsForUser(ctx.userRef, ctx.tenantRef);
    }

    /**
     * For valid sessions, cache the access.
     */
    @Override
    public List<Long> retrieveAllowedRefsCached(final RequestContext ctx) {
        try {
            final Long sessionRef = ctx.internalHeaderParameters.getJwtInfo().getSessionRef();
            if (sessionRef == null) {
                return this.retrieveAllowedRefsUncached(ctx);
            }
            return this.allowedRefsCache.get(sessionRef, () -> this.retrieveAllowedRefsUncached(ctx));
        } catch (ExecutionException e) {
            throw new T9tException(T9tException.ERROR_FILLING_RESTRICTION_CACHE, this.getClass().getCanonicalName());
        }
    }
}
