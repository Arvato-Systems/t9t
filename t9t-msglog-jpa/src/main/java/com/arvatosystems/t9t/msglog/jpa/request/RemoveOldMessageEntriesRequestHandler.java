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
package com.arvatosystems.t9t.msglog.jpa.request;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageEntityResolver;
import com.arvatosystems.t9t.msglog.request.RemoveOldMessageEntriesRequest;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.dp.Jdp;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class RemoveOldMessageEntriesRequestHandler extends AbstractRequestHandler<RemoveOldMessageEntriesRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOldMessageEntriesRequestHandler.class);

    private final IMessageEntityResolver resolver          = Jdp.getRequired(IMessageEntityResolver.class);
    private final IStatisticsService     statisticsService = Jdp.getRequired(IStatisticsService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RemoveOldMessageEntriesRequest request) {
        final boolean isTenantSpecific = !ctx.tenantId.equals(T9tConstants.GLOBAL_TENANT_ID);
        final boolean keepErrorRecords = Boolean.TRUE.equals(request.getKeepErrorRequests());

        final EntityManager em = resolver.getEntityManager();
        String query = "DELETE FROM MessageEntity msg WHERE executionStartedAt < :deleteUntil";

        //if TRUE, do not delete entries which have returnCode >= 200000000 (exception return codes)
        if (keepErrorRecords) {
            query += " AND returnCode < 200000000";
        }
        if (isTenantSpecific) {
            query += " AND tenantId = :tenantId";
        }

        final Query q = em.createQuery(query);

        q.setParameter("deleteUntil", ctx.executionStart.minusSeconds(request.getKeepMaxDaysAge() * 86400L));
        if (isTenantSpecific) {
            q.setParameter("tenantId", ctx.tenantId);
        }

        final int recordsDeleted = q.executeUpdate();
        LOGGER.debug("Deleted {} log_messages records{}", recordsDeleted, keepErrorRecords ? " (but keep error records)" : " (including error records)");

        final Instant exportFinished = Instant.now();
        final StatisticsDTO stat = new StatisticsDTO();
        stat.setRecordsProcessed(recordsDeleted);
        stat.setStartTime(ctx.executionStart);
        stat.setEndTime(exportFinished);
        stat.setJobRef(ctx.internalHeaderParameters.getProcessRef());
        stat.setProcessId(keepErrorRecords ? "delMsgLogOk" : "delMsgLogErr");
        statisticsService.saveStatisticsData(stat);
        return ok();
    }
}
