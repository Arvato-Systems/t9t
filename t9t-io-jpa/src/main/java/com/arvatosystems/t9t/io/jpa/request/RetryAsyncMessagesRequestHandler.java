package com.arvatosystems.t9t.io.jpa.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.search.DummySearchCriteria;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.RetryAsyncMessagesRequest;
import com.arvatosystems.t9t.out.services.IAsyncQueue;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrueFilter;
import de.jpaw.dp.Jdp;

public class RetryAsyncMessagesRequestHandler extends AbstractRequestHandler<RetryAsyncMessagesRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryAsyncMessagesRequestHandler.class);

    private final IAsyncQueueEntityResolver   queueResolver   = Jdp.getRequired(IAsyncQueueEntityResolver.class);
    private final IAsyncMessageEntityResolver messageResolver = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    private final IAsyncQueue                 asyncQueue      = Jdp.getRequired(IAsyncQueue.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, RetryAsyncMessagesRequest request) throws Exception {
        final List<SearchFilter> searchFilters = new ArrayList<>();
        searchFilters.add(rangeFilter("retries", request.getMinRetries(), request.getMaxRetries()));
        searchFilters.add(SearchFilters.equalsFilter("status", ExportStatusEnum.RESPONSE_ERROR.getToken()));
        if (request.getOnlyAsyncQueueRef() != null) {
            final Long queueRef = queueResolver.getRef(request.getOnlyAsyncQueueRef(), false);
            searchFilters.add(SearchFilters.equalsFilter("asyncQueueRef", queueRef));
        }
        if (request.getOnlyAsyncChannelId() != null) {
            searchFilters.add(SearchFilters.equalsFilter("asyncQueueId", request.getOnlyAsyncChannelId()));
        }
        if (request.getOnlyHttpResponseCode() != null) {
            searchFilters.add(SearchFilters.equalsFilter("httpResponseCode", request.getOnlyHttpResponseCode()));
        }
        if (request.getMinRetries() != null) {
            searchFilters.add(SearchFilters.equalsFilter("httpResponseCode", request.getOnlyHttpResponseCode()));
        }
        if (request.getMinAgeInMinutes() != null) {
            final InstantFilter f = new InstantFilter("lastAttempt");
            f.setUpperBound(ctx.executionStart.minusSeconds(60 * request.getMinAgeInMinutes()));
            searchFilters.add(f);
        }
        if (request.getMaxAgeInMinutes() != null) {
            final InstantFilter f = new InstantFilter("lastAttempt");
            f.setUpperBound(ctx.executionStart.minusSeconds(60 * request.getMaxAgeInMinutes()));
            searchFilters.add(f);
        }
        final SearchCriteria criteria = new DummySearchCriteria();
        criteria.setSearchFilter(SearchFilters.and(searchFilters));
        criteria.setSortColumns(Collections.singletonList(new SortColumn("objectRef", false)));
        criteria.setLimit(request.getMaxCount() != null ? request.getMaxCount() : 100000);

        final List<Long> messageRefs = messageResolver.searchKey(criteria);
        LOGGER.info("Async Retry: Found {} messages{}", messageRefs.size());

        // process the data
        int count = 0;
        for (final Long messageRef: messageRefs) {
            // retrieve the full message entity
            final AsyncMessageEntity message = messageResolver.getEntityDataForKey(messageRef, false);
            message.setStatus(ExportStatusEnum.READY_TO_EXPORT);
            if ((++count & 15) == 0) {
                // every 16th we flush to ensure that fast transmissions do not cause race conditions
                messageResolver.getEntityManager().flush();
            }
            asyncQueue.sendAsync(ctx, message.getAsyncChannelId(), message.getPayload(), message.getObjectRef(), 0, null, true);
        }
        return ok();
    }

    private SearchFilter rangeFilter(String name, Integer min, Integer max) {
        if (min == null && max == null) {
            // both are null: no criteria at all
            return new TrueFilter();
        }
        final IntFilter filter = new IntFilter(name);
        filter.setLowerBound(min);
        filter.setUpperBound(max);
        return filter;
    }
}
