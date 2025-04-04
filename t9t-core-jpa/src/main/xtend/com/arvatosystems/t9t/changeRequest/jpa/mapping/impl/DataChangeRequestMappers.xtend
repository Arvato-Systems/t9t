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
package com.arvatosystems.t9t.changeRequest.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO
import com.arvatosystems.t9t.changeRequest.jpa.entities.DataChangeRequestEntity
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IDataChangeRequestEntityResolver

@AutoMap42
class DataChangeRequestMappers {

    IDataChangeRequestEntityResolver entityResolver

    @AutoHandler("SP42")
    def void e2dDataChangeRequestDTO(DataChangeRequestEntity it, DataChangeRequestDTO dto) {
        dto.key = T9tConstants.NO_KEY == key ? null : key
    }

    def void d2eDataChangeRequestDTO(DataChangeRequestEntity entity, DataChangeRequestDTO it) {
        entity.key = key == null ? T9tConstants.NO_KEY : key
    }
}
