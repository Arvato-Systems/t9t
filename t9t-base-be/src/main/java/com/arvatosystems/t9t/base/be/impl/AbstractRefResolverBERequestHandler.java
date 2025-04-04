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
package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.crud.RefResolverRequest;
import com.arvatosystems.t9t.base.crud.RefResolverResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.IRefResolver;

import de.jpaw.bonaparte.pojos.apiw.Ref;

public abstract class AbstractRefResolverBERequestHandler<REF extends Ref, REQUEST extends RefResolverRequest<REF>> extends
        AbstractRequestHandler<REQUEST> {

    @Override
    public boolean isReadOnly(final REQUEST params) {
        return true;
    }

    protected RefResolverResponse execute(final RequestContext ctx, final REQUEST request, final IRefResolver<REF, ?, ?> resolver) {
        final RefResolverResponse rs = new RefResolverResponse();
        rs.setKey(resolver.getRef(request.getRef()));
        rs.setReturnCode(0);
        return rs;
    }
}
