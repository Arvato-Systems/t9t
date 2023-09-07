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
package com.arvatosystems.t9t.updates.be.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTracking;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.T9tCoreException;
import com.arvatosystems.t9t.updates.UpdateStatusDTO;
import com.arvatosystems.t9t.updates.request.PerformReleaseUpdateRequest;
import com.arvatosystems.t9t.updates.request.PerformSingleUpdateRequest;
import com.arvatosystems.t9t.updates.request.UpdateStatusSearchRequest;
import com.arvatosystems.t9t.updates.services.IFeatureUpdate;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class PerformReleaseUpdateRequestHandler extends AbstractRequestHandler<PerformReleaseUpdateRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformReleaseUpdateRequestHandler.class);

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IAutonomousExecutor autonomousExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PerformReleaseUpdateRequest request) throws Exception {
        // obtain all implemented updaters
        final Set<String> allUpdaterQualifiers = Jdp.getQualifiers(IFeatureUpdate.class);
        final List<String> relevantQualifiers = new ArrayList<>(allUpdaterQualifiers.size());
        // filter by desired ones
        final String prefix = request.getPrefix();
        for (final String q: allUpdaterQualifiers) {
            if (q.startsWith(prefix)) {
                relevantQualifiers.add(q);
            }
        }
        final Map<String, UpdateStatusDTO> existing = getIndexedTickets(ctx, prefix);
        LOGGER.info("Selected {} qualifiers out of {} available implementations for release prefix {}, {} existing DB entries",
            relevantQualifiers.size(), allUpdaterQualifiers.size(), prefix, existing.size());

        // sort the changes by timestamp (applySequence)
        Collections.sort(relevantQualifiers);

        // process the updates, each in its own transaction
        for (final String sequenceId: relevantQualifiers) {
            final IFeatureUpdate updateHandler = Jdp.getOptional(IFeatureUpdate.class, sequenceId);
            if (updateHandler == null) {
                LOGGER.error("Failed to obtain implementation of updater for {} - instantiation error!", sequenceId);
                throw new T9tException(T9tCoreException.UPDATE_MISSING_IMPLEMENTATION, sequenceId);
            }
            final String ticketId = updateHandler.getTicketId();
            final UpdateStatusDTO currentStatus = existing.get(ticketId);
            boolean doRun = true;

            if (currentStatus != null) {
                switch (currentStatus.getUpdateApplyStatus()) {
                case COMPLETE:
                    LOGGER.info("Skipping update for ticketId {} - sequence ID {} (already DONE)", ticketId, sequenceId);
                    doRun = false;
                    break;
                case ERROR:
                    break;
                case IN_PROGRESS:
                    LOGGER.info("Skipping update for ticketId {} - sequence ID {} (already DONE)", ticketId, sequenceId);
                    break;
                case NOT_YET_STARTED:
                    break;
                default:
                    break;
                }
            }
            if (doRun) {
                LOGGER.info("Start updating for ticketId {} - sequence ID {}", ticketId, sequenceId);
                final PerformSingleUpdateRequest updateRq = new PerformSingleUpdateRequest();
                updateRq.setTicketId(ticketId);
                final ServiceResponse resp = autonomousExecutor.execute(ctx, updateRq, true);
                if (!ApplicationException.isOk(resp.getReturnCode())) {
                    // complain and terminate
                    LOGGER.error("Update of {} failed with code {} - {}", resp.getReturnCode(), resp.getErrorDetails());
                    return resp;
                }
            }
        }

        return ok();
    }

    private Map<String, UpdateStatusDTO> getIndexedTickets(final RequestContext ctx, final String prefix) {
        final UpdateStatusSearchRequest srq = new UpdateStatusSearchRequest();
        final UnicodeFilter sequenceFilter = new UnicodeFilter("applySequenceId");
        sequenceFilter.setLikeValue(prefix + "%");
        srq.setSearchFilter(sequenceFilter);
        final List<DataWithTrackingS<UpdateStatusDTO, FullTracking>> dwts
          = executor.executeSynchronousAndCheckResult(ctx, srq, ReadAllResponse.class).getDataList();
        if (dwts.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, UpdateStatusDTO> indexed = new HashMap<>(dwts.size());
        for (final DataWithTrackingS<UpdateStatusDTO, FullTracking> dwt: dwts) {
            final UpdateStatusDTO dto = dwt.getData();
            indexed.put(dto.getTicketId(), dto);
        }
        return indexed;
    }
}
