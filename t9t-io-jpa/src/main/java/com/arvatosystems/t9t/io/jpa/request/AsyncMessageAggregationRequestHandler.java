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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.request.AggregationGranularityType;
import com.arvatosystems.t9t.base.request.AggregationResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageStatisticsEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageStatisticsEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncMessageAggregationRequest;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.dp.Jdp;
import jakarta.persistence.Query;

public class AsyncMessageAggregationRequestHandler extends AbstractRequestHandler<AsyncMessageAggregationRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMessageAggregationRequestHandler.class);
    private static final String DEFAULT_REF_TYPE = "-";
    private static final ExportStatusEnum DEFAULT_STATUS_ENUM = ExportStatusEnum.UNDEFINED;
    private static final int DEFAULT_HTTP_RESPONSE_CODE = 0;

    private final IStatisticsService statisticsService = Jdp.getRequired(IStatisticsService.class);

    private static final String DELETE_STATS = "DELETE FROM " + AsyncMessageStatisticsEntity.class.getSimpleName()
        + " s WHERE s.tenantId = :tenantId AND s.slotStart >= :fromDate AND s.slotStart < :toDate";

    private final IAsyncMessageStatisticsEntityResolver resolver = Jdp.getRequired(IAsyncMessageStatisticsEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final AsyncMessageAggregationRequest request) throws Exception {
        // determine effective time range
        final AggregationGranularityType precision = T9tUtil.nvl(request.getAggregationGranularity(), AggregationGranularityType.HOUR);
        final LocalDateTime effectiveIntervalEnd = T9tUtil.truncate(T9tUtil.nvl(request.getEndExcluding(), LocalDateTime.now()), precision);
        final LocalDateTime effectiveIntervalStart = request.getStartIncluding() != null ? T9tUtil.truncate(request.getStartIncluding(), precision)
            : T9tUtil.minusDuration(effectiveIntervalEnd, precision, 1L);
        final Instant effectiveStart = effectiveIntervalStart.toInstant(ZoneOffset.UTC);
        final Instant effectiveEnd = effectiveIntervalEnd.toInstant(ZoneOffset.UTC);
        final AggregationResponse response = new AggregationResponse();

        // Delete existing AsyncMessageStatistics
        final long start = System.currentTimeMillis();
        response.setRecordsDeleted(deleteAsyncMessageStatistics(effectiveStart, effectiveEnd));
        final long mid = System.currentTimeMillis();
        response.setMillisecondsUsedForDeletion(mid - start);

        if (!Boolean.TRUE.equals(request.getOnlyDelete())) {
            // create new AsyncMessageStatistics
            response.setRecordsCreated(createAsyncMessageStatistics(effectiveStart, effectiveEnd, precision));
            final long end = System.currentTimeMillis();
            response.setMillisecondsUsedForDeletion(end - mid);
        }

        writeStatistics(response.getRecordsDeleted(), response.getRecordsCreated(), ctx, precision, effectiveStart, effectiveEnd);
        return response;
    }

    private int deleteAsyncMessageStatistics(final Instant fromDate, final Instant toDate) {
        final Query query = resolver.getEntityManager().createQuery(DELETE_STATS);
        query.setParameter("tenantId", resolver.getSharedTenantId());
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        final int noOfDeletedRows = query.executeUpdate();
        LOGGER.info("{} rows deleted from AsyncMessageStatistics", noOfDeletedRows); // INFO is ok in this case because the frequency is once per hour
        return noOfDeletedRows;
    }

    private int createAsyncMessageStatistics(final Instant fromDate, final Instant toDate, final AggregationGranularityType precision) {
        final String sql = "INSERT INTO " + AsyncMessageStatisticsEntity.TABLE_NAME
            + " SELECT m.tenant_id, MAX(m.object_ref), DATE_TRUNC('" + precision.getToken() + "', m.c_timestamp), m.async_channel_id,"
            + " COALESCE(m.ref_type, '" + DEFAULT_REF_TYPE + "'), COALESCE(m.status, '" + DEFAULT_STATUS_ENUM.getToken() + "'),"
            + " COALESCE(m.http_response_code, " + DEFAULT_HTTP_RESPONSE_CODE + "), COUNT(*), SUM(m.attempts), SUM(COALESCE(m.last_response_time, 0))"
            + " FROM " + AsyncMessageEntity.TABLE_NAME + " m"
            + " WHERE m.tenant_id = :tenantId AND m.c_timestamp >= :fromDate AND m.c_timestamp < :toDate"
            + " GROUP BY m.tenant_id, DATE_TRUNC('" + precision.getToken() + "', m.c_timestamp), m.async_channel_id,"
            + " COALESCE(m.ref_type, '" + DEFAULT_REF_TYPE + "'), COALESCE(m.status, '" + DEFAULT_STATUS_ENUM.getToken() + "'),"
            + " COALESCE(m.http_response_code, " + DEFAULT_HTTP_RESPONSE_CODE + ")";

        final Query query = resolver.getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", resolver.getSharedTenantId());
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        final int noOfInsertedRows = query.executeUpdate();
        LOGGER.info("{} rows inserted in AsyncMessageStatistics", noOfInsertedRows); // INFO is ok in this case because the frequency is once per hour
        return noOfInsertedRows;
    }

    private void writeStatistics(final int deleted, final int inserted, final RequestContext ctx, final AggregationGranularityType precision,
        final Instant effectiveStart, final Instant effectiveEnd) {
        final StatisticsDTO stat = new StatisticsDTO();
        stat.setCount1(inserted);
        stat.setCount2(deleted);
        stat.setInfo1(precision.getToken());
        stat.setInfo2(effectiveStart.toString() + " - " + effectiveEnd.toString());
        stat.setRecordsProcessed(null);
        stat.setStartTime(ctx.executionStart);
        stat.setEndTime(Instant.now());
        stat.setJobRef(ctx.internalHeaderParameters.getProcessRef());
        stat.setProcessId("AsyncMessageAggregation");
        statisticsService.saveStatisticsData(stat);
    }
}
