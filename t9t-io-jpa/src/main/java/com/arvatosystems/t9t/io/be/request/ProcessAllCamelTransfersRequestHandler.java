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
package com.arvatosystems.t9t.io.be.request;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.ProcessAllCamelTransfersRequest;
import com.arvatosystems.t9t.io.request.ProcessCamelRouteRequest;
import com.arvatosystems.t9t.statistics.services.IAutonomousRunner;

import de.jpaw.dp.Jdp;

public class ProcessAllCamelTransfersRequestHandler extends AbstractRequestHandler<ProcessAllCamelTransfersRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessAllCamelTransfersRequestHandler.class);
    protected final IAutonomousRunner runner = Jdp.getRequired(IAutonomousRunner.class);
    protected final ISinkEntityResolver sinkResolver = Jdp.getRequired(ISinkEntityResolver.class);
    protected final IDataSinkEntityResolver dataSinkResolver = Jdp.getRequired(IDataSinkEntityResolver.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, ProcessAllCamelTransfersRequest rq) throws Exception {
        final String variablePart = rq.getOnlySinkId() == null
                ? "WHERE"
                : ", " + dataSinkResolver.getBaseJpaEntityClass().getSimpleName() + " ds WHERE ds.objectRef = s.dataSinkRef AND ds.dataSinkId = :dataSinkId AND";
        final TypedQuery<Long> query = sinkResolver.getEntityManager().createQuery(
            "SELECT s.objectRef FROM " + sinkResolver.getBaseJpaEntityClass().getSimpleName() + " s "
           + variablePart + " s.tenantRef = :tenantRef AND s.cTimestamp < :until AND s.camelTransferStatus IS NOT NULL "
           + "ORDER BY s.cTimestamp", Long.class);
        query.setParameter("tenantRef", sinkResolver.getSharedTenantRef());
        if (rq.getOnlySinkId() != null)
            query.setParameter("dataSinkId", rq.getOnlySinkId());
        int minusMinutes = rq.getMinimumAge() == null ? 60 : rq.getMinimumAge().intValue();
        query.setParameter("until", ctx.executionStart.minus(minusMinutes * 60_000L));
        List<Long> sinkRefs = query.getResultList();

        int numRecords = sinkRefs.size();
        ctx.statusText = "Obtained list of delivery order references: " + numRecords;

        LOGGER.debug("Running {} camel retransfers{}", numRecords, rq.getOnlySinkId());
        runner.runSingleAutonomousTx(ctx, numRecords, sinkRefs,
                ref -> new ProcessCamelRouteRequest(ref),
                stat -> {
                    stat.setCount2(rq.getMinimumAge());  // provide more information what we did...
                    stat.setInfo2(rq.getOnlySinkId());
                },
                "camel-retransfer");
        return ok();
    }
}
