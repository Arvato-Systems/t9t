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

import java.util.List;

import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42;
import com.arvatosystems.t9t.base.search.MassResolverRequest;
import com.arvatosystems.t9t.base.search.MassResolverResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;

public class AbstractMassResolverRequestHandler<S extends MassResolverRequest, E extends BonaPersistableKey<Long> & BonaPersistableTracking<?>> extends AbstractReadOnlyRequestHandler<S> {
    protected final IResolverSurrogateKey42<?, ?, E> resolver;

    protected AbstractMassResolverRequestHandler(IResolverSurrogateKey42<?, ?, E> resolver) {
        this.resolver = resolver;
    }

    @Override
    public MassResolverResponse execute(RequestContext ctx, S rq) {
        final List<Long> refs = resolver.searchKey(rq);
        MassResolverResponse resp = new MassResolverResponse();
        resp.setRefs(refs);
        return resp;
    }
}
