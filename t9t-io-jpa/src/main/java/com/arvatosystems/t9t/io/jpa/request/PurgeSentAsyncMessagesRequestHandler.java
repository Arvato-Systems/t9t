/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncQueueKey;
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.PurgeSentAsyncMessagesRequest;

import de.jpaw.dp.Jdp;

import java.time.Instant;

import jakarta.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeSentAsyncMessagesRequestHandler extends AbstractRequestHandler<PurgeSentAsyncMessagesRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeSentAsyncMessagesRequestHandler.class);

    private final IAsyncMessageEntityResolver messageResolver = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    private final IAsyncQueueEntityResolver queueResolver = Jdp.getRequired(IAsyncQueueEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PurgeSentAsyncMessagesRequest rq) {
        final AsyncQueueEntity queue = queueResolver.getEntityData(new AsyncQueueKey(rq.getAsyncQueueId()), false);
        if (!queue.getTenantId().equals(ctx.tenantId)) {
            throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
        }
        final int maxAge;
        if (rq.getOverrideAge() != null) {
            maxAge = rq.getOverrideAge();
        } else if (queue.getPurgeAfterSeconds() != null) {
            maxAge = queue.getPurgeAfterSeconds();
        } else {
            maxAge = 86400 * 8;
        }
        final Instant purgeAfter = ctx.executionStart.minusSeconds(maxAge);
        LOGGER.info("Purging async sent message of age {} seconds for queue {}", maxAge, queue.getAsyncQueueId());

        final String purge = "DELETE FROM AsyncMessageEntity m WHERE m.tenantId = :tenantId AND m.cTimestamp > :purgeAfter";
        final Query query = messageResolver.getEntityManager().createQuery(purge);
        query.setParameter("tenantId",   ctx.tenantId);
        query.setParameter("purgeAfter", purgeAfter);
        query.executeUpdate();
        return ok();
    }
}
