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

import java.util.List;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageEntityResolver;
import com.arvatosystems.t9t.msglog.request.QueryRequestResultRequest;
import com.arvatosystems.t9t.msglog.request.QueryRequestResultResponse;

import de.jpaw.dp.Jdp;
import jakarta.persistence.TypedQuery;

public class QueryRequestResultRequestHandler extends AbstractReadOnlyRequestHandler<QueryRequestResultRequest> {
    private final IMessageEntityResolver resolver = Jdp.getRequired(IMessageEntityResolver.class);

    @Override
    public QueryRequestResultResponse execute(final RequestContext ctx, final QueryRequestResultRequest request) throws Exception {
        final TypedQuery<Integer> q = resolver.getEntityManager().createQuery(
            "SELECT returnCode FROM " + MessageEntity.class.getSimpleName() + " WHERE messageId = :messageId AND tenantId = :tenantId", Integer.class);
        q.setParameter("tenantId", ctx.tenantId);
        q.setParameter("messageId", request.getMessageId());
        final List<Integer> results = q.getResultList();
        final QueryRequestResultResponse response = new QueryRequestResultResponse();
        if (results.isEmpty()) {
            response.setReturnCode(T9tException.NO_SUCH_REQUEST);
        } else {
            response.setReturnCode(0);
            response.setReturnCodeOfCheckedRequest(results.get(0));
        }
        return response;
    }
}
