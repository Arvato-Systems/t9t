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
package com.arvatosystems.t9t.ssm.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.core.jpa.mapping.ICannedRequestDTOMapper
import com.arvatosystems.t9t.core.jpa.persistence.ICannedRequestEntityResolver
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO
import com.arvatosystems.t9t.ssm.SchedulerSetupKey
import com.arvatosystems.t9t.ssm.jpa.entities.SchedulerSetupEntity
import com.arvatosystems.t9t.ssm.jpa.persistence.ISchedulerSetupEntityResolver

@AutoMap42
class SchedulerSetupEntityMappers {
    ISchedulerSetupEntityResolver   schedulerResolver
    ICannedRequestEntityResolver    requestResolver
    ICannedRequestDTOMapper         requestMapper

    def void e2dSchedulerSetupDTO(SchedulerSetupEntity entity, SchedulerSetupDTO dto) {
        dto.request = requestMapper.mapToDto(entity.cannedRequest)
    }
    def void d2eSchedulerSetupDTO(SchedulerSetupEntity entity, SchedulerSetupDTO dto) {
        entity.request = requestResolver.getRef(dto.request)
    }
    def void e2dSchedulerSetupKey(SchedulerSetupEntity entity, SchedulerSetupKey dto) {}
}
