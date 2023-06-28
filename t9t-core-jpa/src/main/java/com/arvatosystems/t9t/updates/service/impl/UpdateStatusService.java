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
package com.arvatosystems.t9t.updates.service.impl;

import com.arvatosystems.t9t.updates.UpdateApplyStatusType;
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity;
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusLogEntity;
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusLogEntityResolver;
import com.arvatosystems.t9t.updates.service.IUpdateStatusService;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class UpdateStatusService implements IUpdateStatusService {
    protected final IUpdateStatusLogEntityResolver updateStatusLogEntityResolver = Jdp.getRequired(IUpdateStatusLogEntityResolver.class);

    @Override
    public void logUpdateStatus(final UpdateStatusEntity updateStatus) {
        final UpdateStatusLogEntity updateStatusLog = updateStatusLogEntityResolver.newEntityInstance();
        updateStatusLog.setTicketRef(updateStatus.getObjectRef());
        updateStatusLog.setNewStatus(updateStatus.getUpdateApplyStatus());
        updateStatusLogEntityResolver.save(updateStatusLog);
    }

    @Override
    public void updateUpdateStatus(final UpdateStatusEntity updateStatus, final String applySequenceId, final String description,
            final UpdateApplyStatusType updateApplyStatusType) {
        updateStatus.setApplySequenceId(applySequenceId);
        updateStatus.setDescription(description);

        if (updateApplyStatusType != null) {
            updateStatus.setUpdateApplyStatus(updateApplyStatusType);
        }
    }
}
