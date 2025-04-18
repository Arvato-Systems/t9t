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
package com.arvatosystems.t9t.base.jpa.updater;

import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.updater.ReadDataRequest;

import de.jpaw.bonaparte.pojos.api.OperationType;

public class ReadDataRequestHandler extends AbstractDataRequestHandler<ReadDataRequest> {

    @Override
    public CrudAnyKeyResponse execute(final RequestContext ctx, final ReadDataRequest request) {
        final CrudAnyKeyRequest<?, ?> crudRq = getCrudRequest(request.getDtoClassCanonicalName(), request.getKey());
        crudRq.setCrud(OperationType.READ);
        return executor.executeSynchronousAndCheckResult(ctx, crudRq, CrudAnyKeyResponse.class);
    }
}
