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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.DummySearchCriteria;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.IAnyKeySearchRegistry;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.dp.Jdp;

public abstract class AbstractLeanSearchRequestHandler<
  S extends LeanSearchRequest,
  E extends BonaPersistableKey<Long> & BonaPersistableTracking<?>
> extends AbstractReadOnlyRequestHandler<S> {
    protected final IAnyKeySearchRegistry searchRegistry = Jdp.getRequired(IAnyKeySearchRegistry.class);
    protected final IResolverSurrogateKey42<?, ?, E> resolver;
    protected final Function<E, Description> mapper;

    protected AbstractLeanSearchRequestHandler(
            final IResolverSurrogateKey42<?, ?, E> resolver,
            final Function<E, Description> mapper) {
        this.resolver = resolver;
        this.mapper = mapper;
        // dangerous: use self-reference in constructor!
        searchRegistry.registerLeanSearchRequest(this::search, resolver.getRtti(), resolver.getBaseJpaEntityClass().getSimpleName());
    }

    private List<Description> search(final RequestContext ctx, final Long ref) {
        final DummySearchCriteria srq = new DummySearchCriteria();
        final LongFilter objectRefFilter = new LongFilter(T9tConstants.OBJECT_REF_FIELD_NAME);
        objectRefFilter.setEqualsValue(ref);
        srq.setSearchFilter(objectRefFilter);
        return search(ctx, srq);
    }

    private List<Description> search(final RequestContext ctx, final SearchCriteria srq) {
        final List<E> result = resolver.search(srq);
        final List<Description> desc = new ArrayList<>(result.size());
        for (final E e : result) {
            // get a first mapping (id, name)
            final Description d = mapper.apply(e);
            // set common fields...
            d.setObjectRef(e.ret$Key());
            d.setIsActive(e.ret$Active());
            d.setDifferentTenant(!ctx.tenantRef.equals(resolver.getTenantRef(e)));
            // guard for empty description
            if (d.getName() == null)
                d.setName("?");
            desc.add(d);
        }
        return desc;
    }

    @Override
    public LeanSearchResponse execute(final RequestContext ctx, final S rq) {
        final LeanSearchResponse resp = new LeanSearchResponse();
        resp.setDescriptions(search(ctx, rq));
        return resp;
    }
}
