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
package com.arvatosystems.t9t.batch.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.batch.RecordEventsDTO
import com.arvatosystems.t9t.batch.jpa.entities.RecordEventsEntity
import com.arvatosystems.t9t.batch.jpa.persistence.IRecordEventsEntityResolver

@AutoMap42
class RecordEventsMappers {
    IRecordEventsEntityResolver entityResolver
    @AutoHandler("S42")
    def void e2dRecordEventsDTO(RecordEventsEntity entity, RecordEventsDTO dto) {}
}
