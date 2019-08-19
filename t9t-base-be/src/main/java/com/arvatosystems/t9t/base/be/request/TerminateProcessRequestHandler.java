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
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.request.TerminateProcessRequest;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

/**
 * A technical request handler which is used to terminate other processes.
 * The request will return OK if the process existed, an error if it could not be found (maybe was completed in the meantime).
 */
public class TerminateProcessRequestHandler extends AbstractReadOnlyRequestHandler<TerminateProcessRequest> {
    private final RequestContextScope requestContextScope = Jdp.getRequired(RequestContextScope.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final TerminateProcessRequest rq) {
        final ServiceResponse resp = new ServiceResponse();
        if (!T9tConstants.GLOBAL_TENANT_ID.equals(ctx.tenantId) && !rq.getTenantId().equals(ctx.tenantId)) {
            throw new T9tException(T9tException.APP_USER_TENANT_PERMISSION, "No permission to kill process of tenant " + rq.getTenantId());
        }
        resp.setReturnCode(requestContextScope.terminateRequest(rq));
        return resp;
    }
}
