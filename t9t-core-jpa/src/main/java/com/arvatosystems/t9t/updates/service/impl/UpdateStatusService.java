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
