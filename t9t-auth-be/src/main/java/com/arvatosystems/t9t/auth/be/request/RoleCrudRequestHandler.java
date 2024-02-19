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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.RoleDTO;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.request.RoleCrudRequest;
import com.arvatosystems.t9t.auth.services.IRoleResolver;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class RoleCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<RoleRef, RoleDTO, FullTrackingWithVersion, RoleCrudRequest> {

    private final IRoleResolver resolver = Jdp.getRequired(IRoleResolver.class);

    @Override
    public CrudSurrogateKeyResponse<RoleDTO, FullTrackingWithVersion> execute(RequestContext ctx, RoleCrudRequest crudRequest) {
        return execute(ctx, crudRequest, resolver);
    }
}
