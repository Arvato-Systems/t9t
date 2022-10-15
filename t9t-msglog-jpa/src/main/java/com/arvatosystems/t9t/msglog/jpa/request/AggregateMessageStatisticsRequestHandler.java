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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.MessageStatisticsDTO;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageStatisticsEntity;
import com.arvatosystems.t9t.msglog.jpa.mapping.IMessageStatisticsDTOMapper;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageStatisticsEntityResolver;
import com.arvatosystems.t9t.msglog.request.AggregateMessageStatisticsRequest;

import de.jpaw.dp.Jdp;

public class AggregateMessageStatisticsRequestHandler extends AbstractRequestHandler<AggregateMessageStatisticsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMessageStatisticsRequestHandler.class);
    protected final IMessageStatisticsEntityResolver resolver = Jdp.getRequired(IMessageStatisticsEntityResolver.class);
    protected final IMessageStatisticsDTOMapper mapper = Jdp.getRequired(IMessageStatisticsDTOMapper.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final AggregateMessageStatisticsRequest request) throws Exception {
        // Delete existing MessageStatistics
        deleteMessageStatistics(request);

        final List<MessageStatisticsDTO> results = queryMessageStatistics(request);

        // Save MessageStatistics
        for (final MessageStatisticsDTO result : results) {
            final MessageStatisticsEntity entity = mapper.mapToEntity(result, false);
            resolver.save(entity);
        }

        return ok();
    }

    /**
     *
        DELETE
        FROM       p28_dat_message_statistics
        WHERE      day = '2021-01-05'
        ;
     *
     */
    private void deleteMessageStatistics(final AggregateMessageStatisticsRequest request) {
        final LocalDate day = request.getDay() == null ? LocalDate.now().minusDays(1) : request.getDay();

        final StringBuilder sb = new StringBuilder();
        sb.append("DELETE");
        sb.append("  FROM MessageStatisticsEntity ms");
        sb.append(" WHERE ms.day = :day");

        if (request.getUserId() != null) {
            sb.append(" AND ms.userId = :userId");
        }

        if (request.getRequestParameterPqon() != null) {
            sb.append(" AND ms.requestParameterPqon = :requestParameterPqon");
        }

        final Query query = resolver.getEntityManager().createQuery(sb.toString());
        query.setParameter("day", day);

        if (request.getUserId() != null) {
            query.setParameter("userId", request.getUserId());
        }

        if (request.getRequestParameterPqon() != null) {
            query.setParameter("requestParameterPqon", request.getRequestParameterPqon());
        }

        final int noOfDeletedRow = query.executeUpdate();
        LOGGER.debug("{} of rows deleted in MessageStatistics.", noOfDeletedRow);
    }

    /**
     *
        SELECT
                   tenant_id,
                   user_id,
                   request_parameter_pqon,
                   COUNT(CASE WHEN return_code >= 0  AND return_code < 200000000 THEN 1 END) as count_ok,
                   COUNT(CASE WHEN return_code >= 200000000  AND return_code < 1000000000 THEN 1 END) as count_error,
                   MIN(processing_time_in_millisecs) as processing_time_min,
                   MAX(processing_time_in_millisecs) as processing_time_max,
                   SUM(processing_time_in_millisecs) as processing_time_total
        FROM       p28_int_message
        WHERE      return_code is not null
        AND        processing_time_in_millisecs is not null
        AND        execution_started_at >= '2021-01-05'
        AND        execution_started_at < '2021-01-06'
        GROUP BY   tenant_id, user_id, request_parameter_pqon
        ;
     *
     */
    private List<MessageStatisticsDTO> queryMessageStatistics(final AggregateMessageStatisticsRequest request) {
        final LocalDate fromDate = request.getDay() == null ? LocalDate.now().minusDays(1) : request.getDay();
        final LocalDate toDate = fromDate.plusDays(1);
        final String dayStr =  fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        final StringBuilder sb = new StringBuilder();
        sb.append(" SELECT    new com.arvatosystems.t9t.msglog.be.request.MessageStatisticsDTOWrapper(");
        sb.append("               m.tenantId,");
        sb.append("               m.userId,");
        sb.append("               '" + dayStr + "',");
        sb.append("               m.requestParameterPqon,");
        sb.append("               COUNT(CASE WHEN m.returnCode >= 0  AND m.returnCode < 200000000 THEN 1 END),");
        sb.append("               COUNT(CASE WHEN m.returnCode >= 200000000  AND m.returnCode < 1000000000 THEN 1 END),");
        sb.append("               MIN(m.processingTimeInMillisecs),");
        sb.append("               MAX(m.processingTimeInMillisecs),");
        sb.append("               SUM(m.processingTimeInMillisecs)");
        sb.append("           )");
        sb.append(" FROM      MessageEntity m");
        sb.append(" WHERE     m.returnCode IS NOT NULL");
        sb.append(" AND       m.processingTimeInMillisecs IS NOT NULL");
        sb.append(" AND       m.executionStartedAt >= :fromDate");
        sb.append(" AND       m.executionStartedAt < :toDate");

        if (request.getUserId() != null) {
            sb.append(" AND   m.userId = :userId");
        }

        if (request.getRequestParameterPqon() != null) {
            sb.append(" AND   m.requestParameterPqon = :requestParameterPqon");
        }

        sb.append(" GROUP BY  m.tenantId, m.userId, m.requestParameterPqon");

        final TypedQuery<MessageStatisticsDTOWrapper> query = resolver.getEntityManager().createQuery(sb.toString(), MessageStatisticsDTOWrapper.class);
        query.setParameter("fromDate", LocalDateTime.of(fromDate, LocalTime.of(0, 0)).toInstant(ZoneOffset.UTC));
        query.setParameter("toDate", LocalDateTime.of(toDate, LocalTime.of(0, 0)).toInstant(ZoneOffset.UTC));

        if (request.getUserId() != null) {
            query.setParameter("userId", request.getUserId());
        }

        if (request.getRequestParameterPqon() != null) {
            query.setParameter("requestParameterPqon", request.getRequestParameterPqon());
        }

        final List<MessageStatisticsDTOWrapper> dtoWrappers = query.getResultList();

        return dtoWrappers.isEmpty() ? new ArrayList<>() : query.getResultList().stream().map(wrapper -> wrapper.getDto()).collect(Collectors.toList());
    }
}

class MessageStatisticsDTOWrapper {
    private final MessageStatisticsDTO dto;

    MessageStatisticsDTOWrapper(final String tenantId,
      final String userId,
      final String day,
      final String requestParameterPqon,
      final Long countOk,
      final Long countError,
      final Long processingTimeMin,
      final Long processingTimeMax,
      final Long processingTimeTotal) {
        dto = new MessageStatisticsDTO(
            tenantId,
            userId,
            LocalDate.parse(day, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            requestParameterPqon,
            countOk.intValue(),
            countError.intValue(),
            processingTimeMin,
            processingTimeMax,
            processingTimeTotal
        );
    }

    public MessageStatisticsDTO getDto() {
        return dto;
    }
}
