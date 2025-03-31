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
package com.arvatosystems.t9t.msglog.jpa.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity;
import com.arvatosystems.t9t.msglog.request.RerunRequest;

import de.jpaw.util.ApplicationException;

public class RerunRequestHandler extends AbstractRerunRequestHandler<RerunRequest> {

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RerunRequest rq) {
        checkPermission(ctx, rq.ret$PQON());      // additional permission check for CUSTOM and ADMIN

        final MessageEntity loggedRequest = getLoggedRequestByProcessRef(ctx, rq.getProcessRef());
        if (loggedRequest.getRerunByProcessRef() != null) {
            throw new T9tException(T9tException.RERUN_NOT_APPLICABLE_DONE, rq.getProcessRef());
        }
        if (ApplicationException.isOk(loggedRequest.getReturnCode())) {
            throw new T9tException(T9tException.RERUN_NOT_APPLICABLE_RET, rq.getProcessRef());
        }
        // all checks OK: perform the rerun
        final RequestParameters recordedRequest = loggedRequest.getRequestParameters();
        recordedRequest.setMessageId(loggedRequest.getMessageId());
        loggedRequest.setRerunByProcessRef(ctx.getRequestRef());
        executor.executeAsynchronous(ctx, recordedRequest);
        return ok();
    }
}
