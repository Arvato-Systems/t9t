package com.arvatosystems.t9t.updates.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.NeedMapping
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.updates.UpdateStatusDTO
import com.arvatosystems.t9t.updates.UpdateStatusTicketKey
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusEntityResolver

@AutoMap42
class UpdateStatusEntityMapper {
    IUpdateStatusEntityResolver entityResolver

    @AutoHandler("S42")
    def void d2eUpdateStatusDTO(UpdateStatusEntity entity, UpdateStatusDTO dto, boolean onlyActive) {}
    def void e2dUpdateStatusDTO(UpdateStatusEntity entity, UpdateStatusDTO dto) {}

    @NeedMapping  // required because the DTO is final
    def void e2dUpdateStatusTicketKey(UpdateStatusEntity entity, UpdateStatusTicketKey dto) {}
}
