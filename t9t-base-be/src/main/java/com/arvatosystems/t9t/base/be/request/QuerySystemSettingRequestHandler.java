/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.request;

import de.jpaw.bonaparte.pojos.api.OperationType;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.QuerySystemSettingRequest;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public class QuerySystemSettingRequestHandler extends AbstractReadOnlyRequestHandler<QuerySystemSettingRequest> {

    @Override
    public OperationType getAdditionalRequiredPermission(final QuerySystemSettingRequest request) {
        return OperationType.ADMIN;       // must have admin permission to query system settings, as a double check, in addition to the request permissions.
    }

    @Override
    public ServiceResponse execute(final RequestContext ctx, final QuerySystemSettingRequest request) throws Exception {
        final String key = request.getSettingName();
        if (key.startsWith("t9t.restapi.")) {
            return ok();
        }
        throw new T9tException(T9tException.INVALID_PARAMETER, "Not allowed, currently only t9t.restapi.* is allowed");
    }
}
