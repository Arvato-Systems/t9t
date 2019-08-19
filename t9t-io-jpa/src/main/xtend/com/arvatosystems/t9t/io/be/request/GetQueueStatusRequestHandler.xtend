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
package com.arvatosystems.t9t.io.be.request

import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver
import com.arvatosystems.t9t.io.request.AsyncQueueSearchRequest
import com.arvatosystems.t9t.io.request.GetQueueStatusRequest
import com.arvatosystems.t9t.io.request.GetQueueStatusResponse
import com.arvatosystems.t9t.io.request.QueueStatus
import com.arvatosystems.t9t.out.services.IAsyncQueue
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.BooleanFilter
import de.jpaw.dp.Inject
import java.util.ArrayList

@AddLogger
class GetQueueStatusRequestHandler extends AbstractRequestHandler<GetQueueStatusRequest> {
    @Inject IAsyncQueueEntityResolver queueResolver
    @Inject IAsyncQueue queueImpl;

    override execute(RequestContext ctx, GetQueueStatusRequest rq) {
        val resp = new GetQueueStatusResponse
        if (rq.onlyQueue !== null) {
            val queue = queueResolver.getEntityData(rq.onlyQueue, rq.onlyActive);
            resp.status = #[ queueImpl.getQueueStatus(queue.objectRef, queue.asyncQueueId) ]
        } else {
            val refs = queueResolver.search(new AsyncQueueSearchRequest => [
                searchFilter = if (rq.onlyActive) new BooleanFilter("isActive", true)
            ])
            resp.status = new ArrayList<QueueStatus>(refs.size)
            for (ref: refs)
                resp.status.add(queueImpl.getQueueStatus(ref.objectRef, ref.asyncQueueId))
        }
        return resp;
    }
}
