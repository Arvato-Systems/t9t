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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.io.AsyncQueueKey;
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.PurgeSentAsyncMessagesRequest;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.dp.Jdp;

import java.time.Instant;

import jakarta.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeSentAsyncMessagesRequestHandler extends AbstractRequestHandler<PurgeSentAsyncMessagesRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeSentAsyncMessagesRequestHandler.class);
    private static final int SECONDS_IN_A_DAY = 86400;
    private static final int DEFAULT_AGE = 8 * SECONDS_IN_A_DAY;  // by default 8 days

    private final IAsyncMessageEntityResolver messageResolver = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    private final IAsyncQueueEntityResolver queueResolver = Jdp.getRequired(IAsyncQueueEntityResolver.class);
    private final IStatisticsService statisticsService = Jdp.getRequired(IStatisticsService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PurgeSentAsyncMessagesRequest rq) {
        final String queueId = rq.getAsyncQueueId();
        final String channelId = rq.getAsyncChannelId();
        final boolean purgeAll = Boolean.TRUE.equals(rq.getPurgeUnsent());
        final boolean purgeOnlySuccessful = Boolean.TRUE.equals(rq.getOnlySuccessful());
        final Integer overrideAge = rq.getOverrideAge();
        final Long queueRef;
        final int defaultAge;

        final StringBuilder sb = new StringBuilder(200);
        // create the default SQL
        sb.append("DELETE FROM AsyncMessageEntity m WHERE m.tenantId = :tenantId AND m.cTimestamp < :purgeBefore");

        // specific specialization of a queue has been specified
        if (queueId != null) {
            final AsyncQueueEntity queue = queueResolver.getEntityData(new AsyncQueueKey(queueId));
            if (!queue.getTenantId().equals(ctx.tenantId)) {
                throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
            }
            sb.append(" AND m.asyncQueueRef = :queueRef");
            queueRef = queue.getObjectRef();
            defaultAge = queue.getPurgeAfterSeconds() != null ? queue.getPurgeAfterSeconds() : 8 * 86400;
        } else {
            queueRef = null;
            defaultAge = DEFAULT_AGE;
        }
        if (channelId != null) {
            sb.append(" AND m.asyncChannelId = :channelId");
        }

        // variants, specified by option flags
        if (purgeOnlySuccessful) {
            // successfully sent records have a NULL status (to keep the index small)
            sb.append(" AND m.status IS NULL");
        } else if (!purgeAll) {
            // by default, keep records which have never been attempted to send
            sb.append(" AND (status IS NULL OR m.status != '");
            sb.append(ExportStatusEnum.READY_TO_EXPORT.getToken());
            sb.append("')");
        }

        // create and execute the query
        final Query query = messageResolver.getEntityManager().createQuery(sb.toString());
        final Instant maxAgeToKeep = ctx.executionStart.minusSeconds(overrideAge != null ? overrideAge : defaultAge);  // default is 8 days, no queue specific default
        query.setParameter("tenantId", ctx.tenantId);
        query.setParameter("purgeBefore", maxAgeToKeep);
        // add optional parameters
        if (queueRef != null) {
            query.setParameter("queueRef", queueRef);
        }
        if (channelId != null) {
            query.setParameter("channelId", channelId);
        }
        final int numDeleted = query.executeUpdate();

        // set parameters
        LOGGER.info("Purged {} async {}messages older than {} for queue {}, channel {}", numDeleted,
                purgeOnlySuccessful ? "SUCCESSFULLY SENT " : !purgeAll ? "EXCLUDING NOT YET SENT " : "",
                maxAgeToKeep,
                queueId != null ? queueId : "(ALL)",
                channelId != null ? channelId : "(ALL)");

        // write statistics

        final StatisticsDTO stat = new StatisticsDTO();
        stat.setCount1(numDeleted);
        stat.setInfo1(queueId);
        stat.setInfo2(channelId);
        stat.setRecordsProcessed(null);
        stat.setStartTime(ctx.executionStart);
        stat.setEndTime(Instant.now());
        stat.setJobRef(ctx.internalHeaderParameters.getProcessRef());
        stat.setProcessId("PurgeAsyncMessage");
        statisticsService.saveStatisticsData(stat);

        return ok();
    }
}
