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

import java.util.List;

import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.ProcessAllCamelTransfersRequest;
import com.arvatosystems.t9t.io.request.ProcessCamelRouteRequest;
import com.arvatosystems.t9t.statistics.services.IAutonomousRunner;

import de.jpaw.dp.Jdp;

/**
 * Request handler for processing all Camel transfers that require retry or reprocessing.
 *
 * <p>This handler identifies data sinks that have failed or incomplete Camel transfers
 * and schedules them for reprocessing. It queries for sink records that:
 * <ul>
 *   <li>Are output data sinks (not input)</li>
 *   <li>Have a camelTransferStatus indicating processing is needed</li>
 *   <li>Are older than a specified minimum age to avoid processing recent records</li>
 *   <li>Optionally match a specific data sink ID</li>
 * </ul>
 *
 * <p>The processing is performed asynchronously using the autonomous transaction runner
 * to ensure proper error handling and transaction isolation.
 *
 * @see ProcessCamelRouteRequest
 * @see IAutonomousRunner
 */
public class ProcessAllCamelTransfersRequestHandler extends AbstractRequestHandler<ProcessAllCamelTransfersRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessAllCamelTransfersRequestHandler.class);
    private final IAutonomousRunner runner = Jdp.getRequired(IAutonomousRunner.class);
    private final ISinkEntityResolver sinkResolver = Jdp.getRequired(ISinkEntityResolver.class);
    private final IDataSinkEntityResolver dataSinkResolver = Jdp.getRequired(IDataSinkEntityResolver.class);

    /**
     * Processes all eligible Camel transfers for retry.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Builds a query to find sink records that need Camel transfer processing</li>
     *   <li>Applies optional filtering by data sink ID if specified</li>
     *   <li>Filters by minimum age to avoid processing very recent records</li>
     *   <li>Orders results by creation timestamp for consistent processing order</li>
     *   <li>Executes the transfers asynchronously using autonomous transactions</li>
     * </ol>
     *
     * @param ctx the request context containing execution metadata and tenant information
     * @param rq the request containing optional sink ID filter and minimum age settings
     * @return a service response indicating successful initiation of transfer processing
     * @throws Exception if database queries fail or autonomous runner encounters errors
     */
    @Override
    public ServiceResponse execute(final RequestContext ctx, final ProcessAllCamelTransfersRequest rq) throws Exception {
        // Build dynamic query with optional sink ID filter
        final String variablePart = rq.getOnlySinkId() == null ? "" : "AND ds.dataSinkId = :dataSinkId ";
        final TypedQuery<Long> query = sinkResolver.getEntityManager().createQuery(
            "SELECT s.objectRef FROM " + SinkEntity.class.getSimpleName() + " s, " + DataSinkEntity.class.getSimpleName() + " ds "
          + "WHERE  ds.objectRef = s.dataSinkRef "
          + "AND    (ds.isInput = FALSE OR ds.isInput IS NULL) " // Only output sinks
          + variablePart
          + "AND    s.tenantId = :tenantId "
          + "AND    s.cTimestamp < :until "                      // Only records older than minimum age
          + "AND    s.camelTransferStatus IS NOT NULL "          // Only records that need transfer processing
          + "ORDER BY s.cTimestamp", Long.class
        );

        // Set query parameters
        query.setParameter("tenantId", sinkResolver.getSharedTenantId());
        if (rq.getOnlySinkId() != null) {
            query.setParameter("dataSinkId", rq.getOnlySinkId());
        }

        // Calculate timestamp threshold based on minimum age (default 60 minutes)
        final int minusMinutes = rq.getMinimumAge() == null ? 60 : rq.getMinimumAge().intValue();
        query.setParameter("until", ctx.executionStart.minusSeconds(minusMinutes * 60L));
        final List<Long> sinkRefs = query.getResultList();

        final int numRecords = sinkRefs.size();
        ctx.statusText = "Obtained list of data sink references: " + numRecords;

        // Log processing details for debugging
        final String sinkIdStr = rq.getOnlySinkId() != null ? " for sinkId " + rq.getOnlySinkId() : "";
        LOGGER.debug("Running {} camel retransfers{}", numRecords, sinkIdStr);

        // Execute transfers asynchronously with autonomous transactions
        runner.runSingleAutonomousTx(
            ctx, numRecords, sinkRefs,
            ref -> new ProcessCamelRouteRequest(ref),  // Create individual transfer request
            stat -> {
                stat.setCount2(rq.getMinimumAge());  // Track minimum age used
                stat.setInfo2(rq.getOnlySinkId());  // Track optional sink ID filter
            },
            "camel-retransfer"
        );
        return ok();
    }
}
