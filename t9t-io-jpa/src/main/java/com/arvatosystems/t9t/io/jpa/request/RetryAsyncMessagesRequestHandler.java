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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.search.DummySearchCriteria;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAsyncTransmitter;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.RetryAsyncMessagesRequest;
import com.arvatosystems.t9t.io.request.RetryAsyncMessagesResponse;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.dp.Jdp;

public class RetryAsyncMessagesRequestHandler extends AbstractRequestHandler<RetryAsyncMessagesRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryAsyncMessagesRequestHandler.class);

    private final IAsyncQueueEntityResolver   queueResolver     = Jdp.getRequired(IAsyncQueueEntityResolver.class);
    private final IAsyncMessageEntityResolver messageResolver   = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    private final IAsyncTransmitter           asyncTransmitter  = Jdp.getRequired(IAsyncTransmitter.class);
    private final IStatisticsService          statisticsService = Jdp.getRequired(IStatisticsService.class);

    @Override
    public RetryAsyncMessagesResponse execute(RequestContext ctx, RetryAsyncMessagesRequest request) throws Exception {
        final List<SearchFilter> searchFilters = new ArrayList<>();
        final ExportStatusEnum onlyStatus = request.getOnlyStatus() == null
          ? ExportStatusEnum.RESPONSE_ERROR : ExportStatusEnum.valueOf(request.getOnlyStatus());
        final boolean useUnsent = onlyStatus == ExportStatusEnum.READY_TO_EXPORT;
        if (onlyStatus == ExportStatusEnum.RESPONSE_OK) {
            // OK status is mapped to null in the database to avoid index creation
            searchFilters.add(new NullFilter("status"));
        } else {
            searchFilters.add(SearchFilters.equalsFilter("status", onlyStatus.getToken()));
        }
        addRangeFilter(searchFilters, "attempts", request.getMinRetries(), request.getMaxRetries());
        if (request.getOnlyAsyncQueueRef() != null) {
            final Long queueRef = queueResolver.getRef(request.getOnlyAsyncQueueRef());
            searchFilters.add(SearchFilters.equalsFilter("asyncQueueRef", queueRef));
        }
        if (request.getOnlyAsyncChannelId() != null) {
            searchFilters.add(SearchFilters.equalsFilter("asyncChannelId", request.getOnlyAsyncChannelId()));
        }
        if (request.getOnlyHttpResponseCode() != null) {
            searchFilters.add(SearchFilters.equalsFilter("httpResponseCode", request.getOnlyHttpResponseCode()));
        }
        if (request.getMinAgeInMinutes() != null || request.getMaxAgeInMinutes() != null) {
            final InstantFilter f = new InstantFilter("cTimestamp");
            if (request.getMinAgeInMinutes() != null) {
                f.setUpperBound(ctx.executionStart.minusSeconds(60 * request.getMinAgeInMinutes()));
            }
            if (request.getMaxAgeInMinutes() != null) {
                f.setLowerBound(ctx.executionStart.minusSeconds(60 * request.getMaxAgeInMinutes()));
                searchFilters.add(f);
            }
            searchFilters.add(f);
        }
        final SearchCriteria criteria = new DummySearchCriteria();
        criteria.setSearchFilter(SearchFilters.and(searchFilters));
        criteria.setSortColumns(Collections.singletonList(new SortColumn("cTimestamp", true)));
        criteria.setLimit(request.getMaxCount() != null ? request.getMaxCount() : 10000);

        final List<Long> messageRefs = messageResolver.searchKey(criteria);
        LOGGER.info("Async Retry: Found {} messages", messageRefs.size());

        // process the data
        int count = 0;
        for (final Long messageRef: messageRefs) {
            // retrieve the full message entity
            final AsyncMessageEntity message = messageResolver.getEntityDataForKey(messageRef);
            message.setStatus(ExportStatusEnum.READY_TO_EXPORT);
            if ((++count & 15) == 0) {
                // every 16th we flush to ensure that fast transmissions do not cause race conditions
                messageResolver.getEntityManager().flush();
            }
            asyncTransmitter.retransmitMessage(ctx, message.getAsyncChannelId(), message.getPayload(), message.getObjectRef(), 0, null);
        }

        final Instant exportFinished = Instant.now();
        final StatisticsDTO stat = new StatisticsDTO();
        stat.setRecordsProcessed(messageRefs.size());
        stat.setStartTime(ctx.executionStart);
        stat.setEndTime(exportFinished);
        stat.setJobRef(ctx.internalHeaderParameters.getProcessRef());
        stat.setProcessId(useUnsent ? "asyncCatchup" : "asyncRetry");
        statisticsService.saveStatisticsData(stat);

        final RetryAsyncMessagesResponse resp = new RetryAsyncMessagesResponse();
        resp.setNumResent(messageRefs.size());
        return resp;
    }

    private void addRangeFilter(final List<SearchFilter> searchFilters, final String name, final Integer min, final Integer max) {
        if (min == null && max == null) {
            // both are null: no criteria at all
            return;
        }
        final IntFilter filter = new IntFilter(name);
        filter.setLowerBound(min);
        filter.setUpperBound(max);
        searchFilters.add(filter);
    }
}
