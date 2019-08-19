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
package com.arvatosystems.t9t.msglog.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageEntityResolver;
import com.arvatosystems.t9t.msglog.request.RetrieveParametersRequest;
import com.arvatosystems.t9t.msglog.request.RetrieveParametersResponse;

import de.jpaw.dp.Jdp;

public class RetrieveParametersRequestHandler extends AbstractRequestHandler<RetrieveParametersRequest> {
    protected final IMessageEntityResolver resolver = Jdp.getRequired(IMessageEntityResolver.class);

    @Override
    public RetrieveParametersResponse execute(RequestContext ctx, RetrieveParametersRequest rq) {
        final MessageEntity loggedRequest = resolver.find(rq.getProcessRef());
        if (loggedRequest == null)
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, rq.getProcessRef());

        RetrieveParametersResponse resp = new RetrieveParametersResponse();
        if (rq.getRequestParameters())
            resp.setRequestParameters(loggedRequest.getRequestParameters());
        if (rq.getServiceResponse())
            resp.setServiceResponse(loggedRequest.getResponse());
        return resp;
    }
}
