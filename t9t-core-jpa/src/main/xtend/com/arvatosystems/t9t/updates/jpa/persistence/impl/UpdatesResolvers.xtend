package com.arvatosystems.t9t.updates.jpa.persistence.impl

import java.util.List
import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import com.arvatosystems.t9t.updates.UpdateStatusRef
import com.arvatosystems.t9t.updates.UpdateStatusLogRef
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusLogEntity

@AutoResolver42
class UpdatesResolvers {
    def UpdateStatusEntity       getUpdateStatusEntity        (UpdateStatusRef ref, boolean onlyActive) {}
    def UpdateStatusLogEntity    getUpdateStatusLogEntity     (UpdateStatusLogRef ref, boolean onlyActive) {}
    def UpdateStatusEntity       findByTicketId(boolean onlyActive, String ticketId) {}
    def List<UpdateStatusEntity> findByTicketIdsAndUpdateApplyStatus(boolean onlyActive, List<String> ticketId, String updateApplyStatus) {}
}
