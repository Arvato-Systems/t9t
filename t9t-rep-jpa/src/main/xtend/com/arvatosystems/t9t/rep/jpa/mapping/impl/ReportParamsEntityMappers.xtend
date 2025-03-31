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
package com.arvatosystems.t9t.rep.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.rep.ReportParamsDTO
import com.arvatosystems.t9t.rep.ReportParamsKey
import com.arvatosystems.t9t.rep.jpa.entities.ReportParamsEntity
import com.arvatosystems.t9t.rep.jpa.mapping.IReportConfigDTOMapper
import com.arvatosystems.t9t.rep.jpa.persistence.IReportConfigEntityResolver
import com.arvatosystems.t9t.rep.jpa.persistence.IReportParamsEntityResolver
import com.arvatosystems.t9t.doc.jpa.mapping.IMailingGroupDTOMapper
import com.arvatosystems.t9t.doc.jpa.persistence.IMailingGroupEntityResolver

@AutoMap42
class ReportParamsEntityMappers {
    IReportParamsEntityResolver reportParamsResolver
    IReportConfigEntityResolver _er = null
    IReportConfigDTOMapper _em = null
    IMailingGroupEntityResolver mailingGroupEntityResolver
    IMailingGroupDTOMapper mailingGroupDTOMapper

    @AutoHandler("SP42")
    def void e2dReportParamsDTO(ReportParamsEntity entity, ReportParamsDTO dto) {
        dto.reportConfigRef = _em.mapToDto(entity.reportConfigRef)
        if (entity.mailingGroupRef !== null) {
            dto.mailingGroupRef = mailingGroupDTOMapper.mapToDto(entity.mailingGroupRef)
        }
    }
    def void d2eReportParamsDTO(ReportParamsEntity entity, ReportParamsDTO dto) {
        entity.reportConfigRef = _er.getRef(dto.reportConfigRef)
        if (dto.mailingGroupRef !== null) {
            entity.mailingGroupRef = mailingGroupEntityResolver.getRef(dto.mailingGroupRef)
        } else {
            entity.mailingGroupRef = null
        }
    }
    def void e2dReportParamsKey(ReportParamsEntity entity, ReportParamsKey dto) {}
}
