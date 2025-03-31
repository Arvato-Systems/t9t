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
package com.arvatosystems.t9t.msglog.jpa.request;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.request.MessageSearchRequest;
import com.arvatosystems.t9t.msglog.request.RerunFailedRequestsRequest;
import com.arvatosystems.t9t.msglog.request.RerunRequest;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class RerunFailedRequestsRequestHandler extends AbstractRerunRequestHandler<RerunFailedRequestsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RerunFailedRequestsRequestHandler.class);

    private final IAutonomousExecutor autoExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RerunFailedRequestsRequest request) {
        // check ADNMIN and CUSTOM permissions of this request
        checkPermission(ctx, request.ret$PQON());
        // check ADNMIN and CUSTOM permissions of targeted request
        checkPermission(ctx, request.getPqon());

        final List<SearchFilter> filters = new ArrayList<>(8);  // size by max. number of conditions
        // first, add all unconditional filters
        filters.add(SearchFilters.equalsFilter(MessageDTO.meta$$tenantId.getName(), ctx.tenantId));  // only ever rerun requests of the current tenant
        filters.add(SearchFilters.equalsFilter(MessageDTO.meta$$requestParameterPqon.getName(), request.getPqon()));
        filters.add(new NullFilter(MessageDTO.meta$$rerunByProcessRef.getName()));

        // add exactly one type of filter on return code
        if (request.getOnlyReturnCode() != null) {
            filters.add(SearchFilters.equalsFilter(MessageDTO.meta$$returnCode.getName(), request.getOnlyReturnCode()));
        } else if (request.getMinReturnCode() != null || request.getMaxReturnCode() != null) {
            filters.add(createReturnCodeRangeFilter(request.getMinReturnCode(), request.getMaxReturnCode()));
        } else {
            // by default, select every request which did not return OK nor DENIED
            filters.add(createReturnCodeRangeFilter(ApplicationException.CL_PARSER_ERROR * ApplicationException.CLASSIFICATION_FACTOR, null));
        }

        // add conditional filters
        if (request.getOnlyUserId() != null) {
            filters.add(SearchFilters.equalsFilter(MessageDTO.meta$$userId.getName(), request.getOnlyUserId()));
        }
        if (request.getFromDate() != null || request.getToDate() != null) {
            filters.add(createExecutionStartedAtCodeRangeFilter(request.getFromDate(), request.getToDate()));
        }
        final MessageSearchRequest searchRq = new MessageSearchRequest();
        searchRq.setSearchFilter(SearchFilters.and(filters));
        if (request.getMaxCount() != null) {
            searchRq.setLimit(request.getMaxCount());
        }

        // add a defined ordering
        searchRq.setSortColumns(List.of(new SortColumn(T9tConstants.OBJECT_REF_FIELD_NAME, false)));

        // perform the search
        final List<Long> refs = resolver.searchKey(searchRq);
        LOGGER.info("Found {} requests of type {} to rerun", refs.size(), request.getPqon());

        final boolean stopOnError = request.getStopOnError();
        int countOk = 0;
        int countError = 0;
        for (final Long refToRerun: refs) {
            final RerunRequest rerunRequest = new RerunRequest();
            rerunRequest.setProcessRef(refToRerun);
            final int result = autoExecutor.execute(ctx, rerunRequest, false).getReturnCode();
            if (ApplicationException.isOk(result)) {
                ++countOk;
            } else {
                ++countError;
                if (stopOnError) {
                    break;
                }
            }
        }
        LOGGER.info("Completed rerun of requests of type {}: {} OK, {} failed again", request.getPqon(), countOk, countError);

        return ok();
    }

    private SearchFilter createReturnCodeRangeFilter(Integer lowerBound, Integer upperBound) {
        final IntFilter rangeFilter = new IntFilter(MessageDTO.meta$$returnCode.getName());
        rangeFilter.setLowerBound(lowerBound);
        rangeFilter.setUpperBound(upperBound);
        return rangeFilter;
    }

    private SearchFilter createExecutionStartedAtCodeRangeFilter(Instant lowerBound, Instant upperBound) {
        final InstantFilter rangeFilter = new InstantFilter(MessageDTO.meta$$executionStartedAt.getName());
        rangeFilter.setLowerBound(lowerBound);
        rangeFilter.setUpperBound(upperBound);
        return rangeFilter;
    }
}
