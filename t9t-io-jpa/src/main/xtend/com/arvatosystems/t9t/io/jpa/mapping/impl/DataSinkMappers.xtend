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
package com.arvatosystems.t9t.io.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.DataSinkFilterProps
import com.arvatosystems.t9t.io.DataSinkKey
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity
import com.arvatosystems.t9t.io.jpa.mapping.ICsvConfigurationDTOMapper
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkKeyMapper
import com.arvatosystems.t9t.io.jpa.persistence.ICsvConfigurationEntityResolver
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver

@AutoMap42
class DataSinkMappers {
    IDataSinkEntityResolver         entityResolver
    IDataSinkKeyMapper              keyMapper
    ICsvConfigurationEntityResolver csvResolver
    ICsvConfigurationDTOMapper      csvMapper

    def void e2dDataSinkDTO         (DataSinkEntity entity, DataSinkDTO dto) {
        dto.csvConfigurationRef     = csvMapper.mapToDto(entity.csvConfigurationRef)
        dto.responseDataSinkRef     = keyMapper.mapToDto(entity.responseDataSinkRef)
    }

    def void d2eDataSinkDTO         (DataSinkEntity entity, DataSinkDTO dto) {
        entity.csvConfigurationRef = csvResolver.getRef(dto.csvConfigurationRef)
        entity.responseDataSinkRef = entityResolver.getRef(dto.responseDataSinkRef)
    }

    def void e2dDataSinkKey         (DataSinkEntity entity, DataSinkKey dto) {}
    def void e2dDataSinkFilterProps (DataSinkEntity entity, DataSinkFilterProps dto) {}
}
