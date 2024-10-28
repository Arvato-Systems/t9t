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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.T9tCoreException;
import com.arvatosystems.t9t.updates.UpdateApplyStatusType;
import com.arvatosystems.t9t.updates.UpdateStatusTicketKey;
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity;
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusEntityResolver;
import com.arvatosystems.t9t.updates.request.FinishUpdateRequest;
import com.arvatosystems.t9t.updates.service.IUpdateStatusService;
import de.jpaw.dp.Jdp;

public class FinishUpdateRequestHandler extends AbstractRequestHandler<FinishUpdateRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FinishUpdateRequestHandler.class);

    private final IUpdateStatusEntityResolver resolver = Jdp.getRequired(IUpdateStatusEntityResolver.class);
    private final IUpdateStatusService updateStatusService = Jdp.getRequired(IUpdateStatusService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final FinishUpdateRequest request) {
        final String ticketId = request.getTicketId();
        final UpdateStatusEntity updateStatus = resolver.getEntityData(new UpdateStatusTicketKey(ticketId));

        switch (updateStatus.getUpdateApplyStatus()) {
        case IN_PROGRESS:
        case ERROR:
            // OK
            updateStatus.setUpdateApplyStatus(T9tUtil.nvl(request.getNewStatus(), UpdateApplyStatusType.COMPLETE));
            updateStatusService.logUpdateStatus(updateStatus);
            break;
        default:
            LOGGER.error("UpdateApplyStatus of the ticket {} must be IN_PROGRESS or ERROR.", ticketId);
            throw new T9tException(T9tCoreException.FINISH_UPDATE_MUST_BE_IN_PROGRESS, updateStatus.getUpdateApplyStatus());
        }
        return ok();
    }
}
