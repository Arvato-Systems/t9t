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
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.base.request.AggregationGranularityType;
import com.arvatosystems.t9t.base.request.AggregationResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageStatisticsEntity;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageStatisticsEntityResolver;
import com.arvatosystems.t9t.msglog.request.AggregateMessageStatisticsRequest;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.dp.Jdp;
import jakarta.persistence.Query;

public class AggregateMessageStatisticsRequestHandler extends AbstractRequestHandler<AggregateMessageStatisticsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMessageStatisticsRequestHandler.class);

    private final IMessageStatisticsEntityResolver resolver = Jdp.getRequired(IMessageStatisticsEntityResolver.class);
    private final IStatisticsService statisticsService      = Jdp.getRequired(IStatisticsService.class);

    @Override
    public AggregationResponse execute(final RequestContext ctx, final AggregateMessageStatisticsRequest request) throws Exception {
        // determine effective time range
        final AggregationGranularityType precision = T9tUtil.nvl(request.getAggregationGranularity(), AggregationGranularityType.HOUR);
        final LocalDateTime effectiveIntervalEnd = T9tUtil.truncate(T9tUtil.nvl(request.getEndExcluding(), LocalDateTime.now()), precision);
        final LocalDateTime effectiveIntervalStart = request.getStartIncluding() != null
                ? T9tUtil.truncate(request.getStartIncluding(), precision)
                : T9tUtil.minusDuration(effectiveIntervalEnd, precision, 1L);
        final Instant effectiveStart = effectiveIntervalStart.toInstant(ZoneOffset.UTC);
        final Instant effectiveEnd   = effectiveIntervalEnd.toInstant(ZoneOffset.UTC);
        final AggregationResponse response = new AggregationResponse();
        response.setStartIncluding(effectiveIntervalStart);
        response.setEndExcluding(effectiveIntervalEnd);

        // Delete existing MessageStatistics
        final long start = System.currentTimeMillis();
        response.setRecordsDeleted(deleteMessageStatistics(request, effectiveStart, effectiveEnd));
        final long mid = System.currentTimeMillis();
        response.setMillisecondsUsedForDeletion(mid - start);

        if (!Boolean.TRUE.equals(request.getOnlyDelete())) {
            // create new MessageStatistics
            response.setRecordsCreated(createMessageStatistics(request, effectiveStart, effectiveEnd, precision));
            final long end = System.currentTimeMillis();
            response.setMillisecondsUsedForCreation(end - mid);
        }
        writeStatistics(response.getRecordsDeleted(), response.getRecordsCreated(), ctx, precision, effectiveStart, effectiveEnd);
        return response;
    }

    private int createMessageStatistics(final AggregateMessageStatisticsRequest request, final Instant start, final Instant end,
            final AggregationGranularityType precision) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + MessageStatisticsEntity.TABLE_NAME + " ("
            + "object_ref, slot_start, tenant_id, hostname, server_type, partition, transaction_origin_type, user_id, request_parameter_pqon, "
            + "count_ok, count_error, processing_time_max, processing_time_total, processing_delay_max, processing_delay_total"
            + ") SELECT "
            + "MAX(m.object_ref), "
            + "DATE_TRUNC('" + precision.getToken() + "', m.execution_started_at), "
            + "m.tenant_id, "
            + "COALESCE(m.hostname,    '-'), "
            + "COALESCE(m.server_type, '-'), "
            + "COALESCE(m.partition,    99), "
            + "COALESCE(m.transaction_origin_type, '" + TransactionOriginType.OTHER.getToken() + "'), "
            + "m.user_id, "
            + "m.request_parameter_pqon, "
            + "SUM(CASE WHEN m.return_code < 200000000 THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN m.return_code >= 200000000 THEN 1 ELSE 0 END), "
            + "MAX(COALESCE(m.processing_time_in_millisecs, 0)), "
            + "SUM(COALESCE(m.processing_time_in_millisecs, 0)), "
            + "MAX(COALESCE(m.processing_delay_in_millisecs, 0)), "
            + "SUM(COALESCE(m.processing_delay_in_millisecs, 0)) "
            + "FROM " + MessageEntity.TABLE_NAME + " m "
            + "WHERE m.return_code IS NOT NULL AND  m.execution_started_at >= :fromDate AND m.execution_started_at < :toDate");

        if (request.getUserId() != null) {
            sb.append(" AND m.user_id = :userId");
        }

        if (request.getRequestParameterPqon() != null) {
            sb.append(" AND m.request_parameter_pqon = :requestParameterPqon");
        }

        sb.append(" GROUP BY DATE_TRUNC('" + precision.getToken() + "', m.execution_started_at), "
            + "m.tenant_id, m.hostname, m.server_type, m.partition, m.transaction_origin_type, m.user_id, m.request_parameter_pqon");

        final Query query = resolver.getEntityManager().createNativeQuery(sb.toString());
        query.setParameter("fromDate", start);
        query.setParameter("toDate", end);

        if (request.getUserId() != null) {
            query.setParameter("userId", request.getUserId());
        }

        if (request.getRequestParameterPqon() != null) {
            query.setParameter("requestParameterPqon", request.getRequestParameterPqon());
        }

        final int noOfInsertedRows = query.executeUpdate();
        LOGGER.info("{} of rows inserted in MessageStatistics.", noOfInsertedRows);
        return noOfInsertedRows;
    }

    private int deleteMessageStatistics(final AggregateMessageStatisticsRequest request, final Instant start, final Instant end) {
        final StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM MessageStatisticsEntity ms WHERE ms.slotStart >= :whenStart AND ms.slotStart < :whenEnd");

        if (request.getUserId() != null) {
            sb.append(" AND ms.userId = :userId");
        }

        if (request.getRequestParameterPqon() != null) {
            sb.append(" AND ms.requestParameterPqon = :requestParameterPqon");
        }

        final Query query = resolver.getEntityManager().createQuery(sb.toString());
        query.setParameter("whenStart", start);
        query.setParameter("whenEnd", end);

        if (request.getUserId() != null) {
            query.setParameter("userId", request.getUserId());
        }

        if (request.getRequestParameterPqon() != null) {
            query.setParameter("requestParameterPqon", request.getRequestParameterPqon());
        }

        final int noOfDeletedRows = query.executeUpdate();
        LOGGER.info("{} of rows deleted in MessageStatistics.", noOfDeletedRows);
        return noOfDeletedRows;
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
        stat.setProcessId("MessageAggregation");
        statisticsService.saveStatisticsData(stat);
    }
}
