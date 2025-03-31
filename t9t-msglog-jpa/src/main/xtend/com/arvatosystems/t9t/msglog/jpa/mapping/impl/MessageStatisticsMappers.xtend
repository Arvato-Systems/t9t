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
package com.arvatosystems.t9t.msglog.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.msglog.MessageStatisticsDTO
import com.arvatosystems.t9t.msglog.jpa.entities.MessageStatisticsEntity
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageStatisticsEntityResolver

@AutoMap42
class MessageStatisticsMappers {
    IMessageStatisticsEntityResolver resolver

    def void e2dMessageStatisticsDTO(MessageStatisticsEntity it, MessageStatisticsDTO dto) {
        dto.processingTimeAvg  = processingTimeTotal  as double / (countError + countOk)
        dto.processingDelayAvg = processingDelayTotal as double / (countError + countOk)
    }

    def void d2eMessageStatisticsDTO(MessageStatisticsEntity it, MessageStatisticsDTO dto) {}
}
