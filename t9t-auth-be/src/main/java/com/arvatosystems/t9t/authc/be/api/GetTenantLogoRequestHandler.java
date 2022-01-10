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
package com.arvatosystems.t9t.authc.be.api;

import com.arvatosystems.t9t.auth.services.ITenantLogoDtoResolver;
import com.arvatosystems.t9t.authc.api.GetTenantLogoRequest;
import com.arvatosystems.t9t.authc.api.GetTenantLogoResponse;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.dp.Jdp;

public class GetTenantLogoRequestHandler extends AbstractReadOnlyRequestHandler<GetTenantLogoRequest> {

    private final ITenantLogoDtoResolver tenantLogoResolver = Jdp.getRequired(ITenantLogoDtoResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final GetTenantLogoRequest request) throws Exception {
        final GetTenantLogoResponse response = new GetTenantLogoResponse();
        response.setTenantLogo(tenantLogoResolver.getModuleConfiguration().getLogo());
        return response;
    }
}
