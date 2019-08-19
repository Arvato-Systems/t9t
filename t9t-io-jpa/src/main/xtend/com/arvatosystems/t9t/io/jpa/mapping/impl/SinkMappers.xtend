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
package com.arvatosystems.t9t.io.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.io.SinkDTO
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkFilterPropsMapper
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver

@AutoMap42
class SinkMappers {
    ISinkEntityResolver entityResolver
    IDataSinkEntityResolver sinkResolver
    IDataSinkFilterPropsMapper sinkMapper

    @AutoHandler("R42")
    def void d2eSinkDTO(SinkEntity entity, SinkDTO dto, boolean onlyActive) {
        entity.dataSinkRef = sinkResolver.getRef(dto.dataSinkRef, onlyActive)
    }
    def void e2dSinkDTO(SinkEntity entity, SinkDTO dto) {
        dto.dataSinkRef = sinkMapper.mapToDto(entity.dataSinkRef)
    }
}
