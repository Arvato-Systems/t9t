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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncQueueSearchRequest;
import com.arvatosystems.t9t.io.request.GetQueueStatusRequest;
import com.arvatosystems.t9t.io.request.GetQueueStatusResponse;
import com.arvatosystems.t9t.io.request.QueueStatus;
import com.arvatosystems.t9t.out.services.IAsyncQueue;

import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.dp.Jdp;

import java.util.ArrayList;
import java.util.List;

public class GetQueueStatusRequestHandler extends AbstractRequestHandler<GetQueueStatusRequest> {
    private final IAsyncQueueEntityResolver queueResolver = Jdp.getRequired(IAsyncQueueEntityResolver.class);
    private final IAsyncQueue queueImpl = Jdp.getRequired(IAsyncQueue.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final GetQueueStatusRequest rq) {
        final GetQueueStatusResponse resp = new GetQueueStatusResponse();
        if (rq.getOnlyQueue() != null) {
            final AsyncQueueEntity queue = queueResolver.getEntityData(rq.getOnlyQueue());
            final List<QueueStatus> status = new ArrayList<>(1);
            status.add(queueImpl.getQueueStatus(queue.getObjectRef(), queue.getAsyncQueueId()));
            resp.setStatus(status);
        } else {
            final AsyncQueueSearchRequest asyncQueueSearchRequest = new AsyncQueueSearchRequest();
            if (rq.getOnlyActive()) {
                asyncQueueSearchRequest.setSearchFilter(new BooleanFilter("isActive", true));
            }
            final List<AsyncQueueEntity> refs = queueResolver.search(asyncQueueSearchRequest);
            resp.setStatus(new ArrayList<>(refs.size()));
            for (final AsyncQueueEntity ref : refs) {
                resp.getStatus().add(queueImpl.getQueueStatus(ref.getObjectRef(), ref.getAsyncQueueId()));
            }
        }
        return resp;
    }
}
