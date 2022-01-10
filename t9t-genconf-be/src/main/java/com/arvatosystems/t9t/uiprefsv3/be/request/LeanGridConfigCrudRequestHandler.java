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
package com.arvatosystems.t9t.uiprefsv3.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigDTO;
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigRef;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigCrudRequest;
import com.arvatosystems.t9t.uiprefsv3.services.ILeanGridConfigResolver;

import de.jpaw.dp.Jdp;

public class LeanGridConfigCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<LeanGridConfigRef, LeanGridConfigDTO,
  FullTrackingWithVersion, LeanGridConfigCrudRequest> {

    // @Inject
    protected final ILeanGridConfigResolver resolver = Jdp.getRequired(ILeanGridConfigResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final LeanGridConfigCrudRequest request) throws Exception {
        return execute(ctx, request, resolver);
    }
}
