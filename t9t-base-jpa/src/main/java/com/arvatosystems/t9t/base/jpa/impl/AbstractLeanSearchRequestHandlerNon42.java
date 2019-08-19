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

import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;

public class AbstractLeanSearchRequestHandlerNon42 <S extends LeanSearchRequest, E extends BonaPersistableKey<Long> & BonaPersistableTracking<?>> extends AbstractReadOnlyRequestHandler<S> {
    protected final IResolverSurrogateKey<?, ?, E> resolver;
    protected final Function<E, Description> mapper;

    protected AbstractLeanSearchRequestHandlerNon42(
            IResolverSurrogateKey<?, ?, E> resolver,
            Function<E, Description> mapper) {
        this.resolver = resolver;
        this.mapper = mapper;
    }

    @Override
    public LeanSearchResponse execute(RequestContext ctx, S rq) {
        final List<E> result = resolver.search(rq);
        final List<Description> desc = new ArrayList<Description>(result.size());
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
            desc.add(d);
        }
        LeanSearchResponse resp = new LeanSearchResponse();
        resp.setDescriptions(desc);
        return resp;
    }
}
