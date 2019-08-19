/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.rep.ReportParamsDTO
import com.arvatosystems.t9t.rep.ReportParamsKey
import com.arvatosystems.t9t.rep.jpa.entities.ReportParamsEntity
import com.arvatosystems.t9t.rep.jpa.mapping.IReportConfigDTOMapper
import com.arvatosystems.t9t.rep.jpa.mapping.IReportMailingDTOMapper
import com.arvatosystems.t9t.rep.jpa.persistence.IReportConfigEntityResolver
import com.arvatosystems.t9t.rep.jpa.persistence.IReportMailingEntityResolver
import com.arvatosystems.t9t.rep.jpa.persistence.IReportParamsEntityResolver

@AutoMap42
public class ReportParamsEntityMappers {
    IReportParamsEntityResolver reportParamsResolver
    IReportConfigEntityResolver _er = null
    IReportConfigDTOMapper _em = null
    IReportMailingEntityResolver reportMailingEntityResolver
    IReportMailingDTOMapper reportMailingDTOMapper

    @AutoHandler("ASP42")
    def void e2dReportParamsDTO(ReportParamsEntity entity, ReportParamsDTO dto) {
        dto.reportConfigRef = _em.mapToDto(entity.reportConfigRef)
        if (entity.mailingGroupRef !== null) {
            dto.mailingGroupRef = reportMailingDTOMapper.mapToDto(entity.mailingGroupRef)
        }
    }
    def void d2eReportParamsDTO(ReportParamsEntity entity, ReportParamsDTO dto, boolean onlyActive) {
        entity.reportConfigRef = _er.getRef(dto.reportConfigRef, onlyActive)
        if (dto.mailingGroupRef !== null) {
            entity.mailingGroupRef = reportMailingEntityResolver.getRef(dto.mailingGroupRef, onlyActive)
        } else {
            entity.mailingGroupRef = null
        }
    }
    def void e2dReportParamsKey(ReportParamsEntity entity, ReportParamsKey dto) {}
}
