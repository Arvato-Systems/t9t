/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.util.List;

import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey;
import com.arvatosystems.t9t.base.search.MassResolverRequest;
import com.arvatosystems.t9t.base.search.MassResolverResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;

public abstract class AbstractMassResolverRequestHandler<
  S extends MassResolverRequest,
  E extends BonaPersistableKey<Long> & BonaPersistableTracking<?>
> extends AbstractReadOnlyRequestHandler<S> {
    protected final IResolverSurrogateKey<?, ?, E> resolver;

    protected AbstractMassResolverRequestHandler(final IResolverSurrogateKey<?, ?, E> resolver) {
        this.resolver = resolver;
    }

    @Override
    public MassResolverResponse execute(final RequestContext ctx, final S rq) {
        final List<Long> refs = resolver.searchKey(rq);
        final MassResolverResponse resp = new MassResolverResponse();
        resp.setRefs(refs);
        return resp;
    }
}
