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
package com.arvatosystems.t9t.authc.be.api;

import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.authc.api.GetTenantsRequest;
import com.arvatosystems.t9t.authc.api.GetTenantsResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.dp.Jdp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTenantsRequestHandler extends AbstractReadOnlyRequestHandler<GetTenantsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetTenantsRequestHandler.class);

    private final IAuthPersistenceAccess authPersistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);

    @Override
    public GetTenantsResponse execute(final RequestContext ctx, final GetTenantsRequest rq) throws Exception {
        final GetTenantsResponse it = new GetTenantsResponse();
        it.setTenants(authPersistenceAccess.getAllTenantsForUser(ctx, ctx.userRef));
        LOGGER.debug("Got {} tenants", it.getTenants().size());
        return it;
    }
}
