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
package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.AsyncBatchRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;

import de.jpaw.dp.Jdp;

public class AsyncBatchRequestHandler extends AbstractRequestHandler<AsyncBatchRequest>  {

    // @Inject
    protected final IExecutor messaging = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(AsyncBatchRequest request) {
        ServiceResponse resp = messaging.executeSynchronous(request.getPrimaryRequest());
        if (resp.getReturnCode() <= (request.getAllowNo() ? T9tConstants.MAX_DECLINE_RETURN_CODE : T9tConstants.MAX_OK_RETURN_CODE)) {
            // fine (preliminary check), secondary will be done after commit, to capture late exceptions
            messaging.executeAsynchronous(request.getAsyncRequest());
        }
        return resp;
    }
}
