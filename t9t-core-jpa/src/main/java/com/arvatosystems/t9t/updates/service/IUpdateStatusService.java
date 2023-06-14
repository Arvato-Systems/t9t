package com.arvatosystems.t9t.updates.service;

import com.arvatosystems.t9t.updates.UpdateApplyStatusType;
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity;

public interface IUpdateStatusService {
    void logUpdateStatus(UpdateStatusEntity updateStatus);

    void updateUpdateStatus(UpdateStatusEntity updateStatus, String applySequenceId,
            String description, UpdateApplyStatusType updateApplyStatusType);
}
