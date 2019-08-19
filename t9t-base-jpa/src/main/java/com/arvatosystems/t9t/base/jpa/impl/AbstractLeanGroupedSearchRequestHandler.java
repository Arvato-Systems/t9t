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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.DescriptionList;
import com.arvatosystems.t9t.base.search.LeanGroupedSearchRequest;
import com.arvatosystems.t9t.base.search.LeanGroupedSearchResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;

public class AbstractLeanGroupedSearchRequestHandler<S extends LeanGroupedSearchRequest, E extends BonaPersistableKey<Long> & BonaPersistableTracking<?>> extends AbstractReadOnlyRequestHandler<S> {
    protected final IResolverSurrogateKey42<?, ?, E> resolver;
    protected final Function<E, Long> getGroup;
    protected final Function<E, Description> mapper;

    protected AbstractLeanGroupedSearchRequestHandler(
            IResolverSurrogateKey42<?, ?, E> resolver,
            Function<E, Long> getGroup,
            Function<E, Description> mapper) {
        this.resolver = resolver;
        this.getGroup = getGroup;
        this.mapper = mapper;
    }

    @Override
    public LeanGroupedSearchResponse execute(RequestContext ctx, S rq) {
        final List<E> result = resolver.search(rq);
        final Map<Long, DescriptionList> results = new HashMap<>();
        for (E e : result) {
            // get a first mapping (id, name)
            final Description d = mapper.apply(e);
            // set common fields...
            d.setObjectRef(e.ret$Key());
            d.setIsActive(e.ret$Active());
            d.setDifferentTenant(!ctx.tenantRef.equals(resolver.getTenantRef(e)));
            // guard for empty description
            if (d.getName() == null)
                d.setName("?");
            results.computeIfAbsent(getGroup.apply(e),
                (x) -> new DescriptionList(new ArrayList<Description>())
            ).getDescriptions().add(d);
        }
        LeanGroupedSearchResponse resp = new LeanGroupedSearchResponse();
        resp.setResults(results);
        return resp;
    }
}
