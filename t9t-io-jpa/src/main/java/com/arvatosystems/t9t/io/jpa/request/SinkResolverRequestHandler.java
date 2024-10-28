/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.crud.RefResolverResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.SinkResolverRequest;

import de.jpaw.dp.Jdp;

public class SinkResolverRequestHandler extends AbstractRequestHandler<SinkResolverRequest> {

    private final ISinkEntityResolver resolver = Jdp.getRequired(ISinkEntityResolver.class);

    @Override
    public RefResolverResponse execute(final RequestContext ctx, final SinkResolverRequest request) throws Exception {
        final Long ref = resolver.getRef(request.getRef());
        final RefResolverResponse resp = new RefResolverResponse();
        resp.setKey(ref);
        resp.setReturnCode(0);
        return resp;
    }
}
