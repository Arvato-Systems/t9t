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

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.io.AsyncQueueKey
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver
import com.arvatosystems.t9t.io.request.PurgeSentAsyncMessagesRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class PurgeSentAsyncMessagesRequestHandler extends AbstractRequestHandler<PurgeSentAsyncMessagesRequest> {
    @Inject IAsyncMessageEntityResolver messageResolver
    @Inject IAsyncQueueEntityResolver queueResolver

    override ServiceResponse execute(RequestContext ctx, PurgeSentAsyncMessagesRequest rq) {
        val queue = queueResolver.getEntityData(new AsyncQueueKey(rq.asyncQueueId), false)
        if (queue.tenantRef != ctx.tenantRef)
            throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT)
        val maxAge = (rq.overrideAge ?: queue.purgeAfterSeconds) ?: 86400 * 8;
        val purgeAfter = ctx.executionStart.minus(1000L * maxAge)
        LOGGER.info("Purging async sent message of age {} seconds for queue {}", maxAge, queue.asyncQueueId)

        val purge = "DELETE FROM AsyncMessageEntity m WHERE m.tenantRef = :tenantRef AND m.cTimestamp > :purgeAfter";
        val query = messageResolver.entityManager.createQuery(purge)
        query.setParameter("tenantRef", ctx.tenantRef)
        query.setParameter("purgeAfter", purgeAfter)
        query.executeUpdate();
        return ok
    }
}
