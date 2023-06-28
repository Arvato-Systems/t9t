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
package com.arvatosystems.t9t.batch.jpa.request;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.request.AggregationGranularityType;
import com.arvatosystems.t9t.base.services.AbstractAggregationRequestHandler;
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsAggregationEntity;
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsEntity;
import com.arvatosystems.t9t.batch.jpa.persistence.IStatisticsAggregationEntityResolver;
import com.arvatosystems.t9t.batch.request.AggregateMessageStatisticsRequest;

import de.jpaw.dp.Jdp;
import jakarta.persistence.Query;

public class AggregateMessageStatisticsRequestHandler extends AbstractAggregationRequestHandler<AggregateMessageStatisticsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMessageStatisticsRequestHandler.class);

    private static final String DELETE_AGGREGATION = "DELETE FROM " + StatisticsAggregationEntity.class.getSimpleName()
        + " a WHERE a.tenantId = :tenantId AND a.slotStart >= :fromDate AND a.slotStart < :toDate";

    private final IStatisticsAggregationEntityResolver resolver = Jdp.getRequired(IStatisticsAggregationEntityResolver.class);

    @Override
    protected int deleteAggregation(final Instant fromDate, final Instant toDate) {
        final Query query = resolver.getEntityManager().createQuery(DELETE_AGGREGATION);
        query.setParameter("tenantId", resolver.getSharedTenantId());
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        final int noOfDeletedRows = query.executeUpdate();
        LOGGER.info("{} rows deleted from StatisticsAggregationEntity", noOfDeletedRows);
        return noOfDeletedRows;
    }

    @Override
    protected int createAggregation(final Instant fromDate, final Instant toDate, final AggregationGranularityType precision) {
        final String sql = "INSERT INTO " + StatisticsAggregationEntity.TABLE_NAME + " SELECT s.tenant_id, MAX(s.object_ref), s.process_id, DATE_TRUNC('"
            + precision.getToken() + "', s.start_time), COUNT(*), COALESCE(SUM(s.records_processed), 0), COALESCE(SUM(s.records_error), 0),"
            + " COALESCE(SUM(s.count1), 0), COALESCE(SUM(s.count2), 0), COALESCE(SUM(s.count3), 0), COALESCE(SUM(s.count4), 0) FROM "
            + StatisticsEntity.TABLE_NAME + " s WHERE s.tenant_id = :tenantId AND s.start_time >= :fromDate AND s.start_time < :toDate"
            + " GROUP BY s.tenant_id, s.process_id, DATE_TRUNC('" + precision.getToken() + "', s.start_time)";

        final Query query = resolver.getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", resolver.getSharedTenantId());
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        final int noOfInsertedRows = query.executeUpdate();
        LOGGER.info("{} rows inserted in StatisticsAggregationEntity", noOfInsertedRows);
        return noOfInsertedRows;
    }
}
