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
package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.crud.RefResolverRequest;
import com.arvatosystems.t9t.base.crud.RefResolverResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;

import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.bonaparte.refsw.RefResolver;

public abstract class AbstractRefResolverBERequestHandler<REF extends Ref, REQUEST extends RefResolverRequest<REF>> extends
        AbstractRequestHandler<REQUEST> {

    @Override
    public boolean isReadOnly(REQUEST params) {
        return true;
    }

    protected RefResolverResponse execute(REQUEST request, RefResolver<REF, ?, ?> resolver) {
        RefResolverResponse rs = new RefResolverResponse();
        rs.setKey(resolver.getRef(request.getRef()));
        rs.setReturnCode(0);
        return rs;
    }
}
