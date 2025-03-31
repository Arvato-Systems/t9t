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
package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.AsyncBatchRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class AsyncBatchRequestHandler extends AbstractRequestHandler<AsyncBatchRequest>  {

    private final IExecutor messaging = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final AsyncBatchRequest request) {
        // validate permission of async request first
        final ServiceResponse errorResp = messaging.permissionCheck(ctx, request.getAsyncRequest());
        if (errorResp != null) {
            return errorResp;
        }

        final ServiceResponse resp = messaging.executeSynchronousWithPermissionCheck(ctx, request.getPrimaryRequest());
        if (resp.getReturnCode() <= (request.getAllowNo() ? T9tConstants.MAX_DECLINE_RETURN_CODE : T9tConstants.MAX_OK_RETURN_CODE)) {
            // fine (preliminary check), secondary will be done after commit, to capture late exceptions
            messaging.executeAsynchronous(ctx, request.getAsyncRequest());
        }
        return resp;
    }
}
