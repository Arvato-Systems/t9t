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
package com.arvatosystems.t9t.updates.jpa.request;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.T9tCoreException;
import com.arvatosystems.t9t.updates.UpdateApplyStatusType;
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity;
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusEntityResolver;
import com.arvatosystems.t9t.updates.request.StartUpdateRequest;
import com.arvatosystems.t9t.updates.service.IUpdateStatusService;
import de.jpaw.dp.Jdp;

public class StartUpdateRequestHandler extends AbstractRequestHandler<StartUpdateRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartUpdateRequestHandler.class);

    protected final IUpdateStatusEntityResolver resolver = Jdp.getRequired(IUpdateStatusEntityResolver.class);
    protected final IUpdateStatusService updateStatusService = Jdp.getRequired(IUpdateStatusService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final StartUpdateRequest request) {

        validatePrerequisitesIfAny(request);

        final String ticketId = request.getTicketId();
        final String applySequenceId = request.getApplySequenceId();
        final String description = request.getDescription();

        final UpdateStatusEntity updateStatus = resolver.findByTicketId(true, ticketId);
        if (updateStatus == null) {
            LOGGER.debug("No ticket update status found, create a new entry for ticket {}", ticketId);
            final UpdateStatusEntity newUpdateStatus = resolver.newEntityInstance();
            newUpdateStatus.setTicketId(ticketId);
            updateStatusService.updateUpdateStatus(newUpdateStatus, applySequenceId, description, UpdateApplyStatusType.NOT_YET_STARTED);
            resolver.save(newUpdateStatus);

            updateStatusService.logUpdateStatus(newUpdateStatus);
        } else {
            final UpdateApplyStatusType currentUpdateApplyStatus = updateStatus.getUpdateApplyStatus();
            switch (currentUpdateApplyStatus) {
            case NOT_YET_STARTED:
                LOGGER.debug("Ticket is not yet started, starting the ticket {}", ticketId);
                updateStatusService.updateUpdateStatus(updateStatus, applySequenceId, description, UpdateApplyStatusType.IN_PROGRESS);
                updateStatusService.logUpdateStatus(updateStatus);
                break;
            case IN_PROGRESS: {
                if (request.getAllowRestartOfPending()) {
                    LOGGER.debug("Ticket is in progress and allowRestartOfPending flag is true. Updating the ticket {}", ticketId);
                    updateStatusService.updateUpdateStatus(updateStatus, applySequenceId, description, null);
                    updateStatusService.logUpdateStatus(updateStatus);
                    break;
                } else {
                    LOGGER.error("Ticket is in progress and allowRestartOfPending flag is false. Throwing exception for the ticket {}", ticketId);
                    throw new T9tException(T9tCoreException.UPDATE_STATUS_ALREADY_IN_PROGRESS,
                            "Ticket status is " + updateStatus.getUpdateApplyStatus() + " but not "
                                    + UpdateApplyStatusType.IN_PROGRESS + ".");
                }
            }
            case COMPLETE:
                LOGGER.error("Ticket is not yet started, starting the ticket {}", ticketId);
                throw new T9tException(T9tCoreException.UPDATE_STATUS_INVALID_STATE, "The ticket has been completed.");
            default:
                LOGGER.error("Invalid currentUpdateApplyStatus {} for the ticket {}", currentUpdateApplyStatus, ticketId);
                throw new UnsupportedOperationException();
            }
        }
        return ok();
    }

    private void validatePrerequisitesIfAny(final StartUpdateRequest request) {
        final List<String> prerequisites = request.getPrerequisites();
        final String ticketId = request.getTicketId();
        if (prerequisites == null || prerequisites.isEmpty()) {
            return;
        }

        final List<UpdateStatusEntity> prerequisiteUpdateStatuses = resolver.findByTicketIdsAndUpdateApplyStatus(true,
                prerequisites, UpdateApplyStatusType.COMPLETE.getToken());
        if (prerequisiteUpdateStatuses.size() != prerequisites.size()) {
            LOGGER.error("Not all prerequisite tickets are completed, ticket {}", ticketId);
            throw new T9tException(T9tCoreException.UPDATE_STATUS_PREREQUISITES, "Not all prerequisite tickets are completed.");
        }
    }
}
