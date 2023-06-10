/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
import com.arvatosystems.t9t.io.AsyncMessageStatisticsDTO
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageStatisticsEntity
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageStatisticsEntityResolver

@AutoMap42
class AsyncMessageStatisticsMappers {
    IAsyncMessageStatisticsEntityResolver resolver

    def void e2dAsyncMessageStatisticsDTO(AsyncMessageStatisticsEntity entity, AsyncMessageStatisticsDTO dto) {
        // construct the virtual fields
        dto.numberOfRetries = entity.attempts - entity.count
        if (entity.count != 0) {
            dto.averageResponseTime = (entity.responseTime as double) / entity.count
        }
    }
    def void d2eAsyncMessageStatisticsDTO(AsyncMessageStatisticsEntity entity, AsyncMessageStatisticsDTO dto, boolean onlyActive) {}
}
