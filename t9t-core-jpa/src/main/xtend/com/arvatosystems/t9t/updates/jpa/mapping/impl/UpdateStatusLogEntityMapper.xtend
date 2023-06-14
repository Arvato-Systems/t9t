package com.arvatosystems.t9t.updates.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.updates.UpdateStatusLogDTO
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusLogEntity
import com.arvatosystems.t9t.updates.jpa.mapping.IUpdateStatusTicketKeyMapper
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusLogEntityResolver

@AutoMap42
class UpdateStatusLogEntityMapper {
    IUpdateStatusLogEntityResolver entityResolver
    IUpdateStatusTicketKeyMapper ticketKeyMapper

    @AutoHandler("S42")
    def void d2eUpdateStatusLogDTO(UpdateStatusLogEntity entity, UpdateStatusLogDTO dto, boolean onlyActive) {}
    def void e2dUpdateStatusLogDTO(UpdateStatusLogEntity entity, UpdateStatusLogDTO dto) {
        dto.ticketRef = ticketKeyMapper.mapToDto(entity.ticket)
    }
}
