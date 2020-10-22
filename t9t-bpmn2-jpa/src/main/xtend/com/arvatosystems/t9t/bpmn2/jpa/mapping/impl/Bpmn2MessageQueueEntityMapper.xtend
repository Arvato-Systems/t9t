/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.bpmn2.Bpmn2MessageQueueDTO
import com.arvatosystems.t9t.bpmn2.jpa.entities.Bpmn2MessageQueueEntity
import com.arvatosystems.t9t.bpmn2.jpa.persistence.IBpmn2MessageQueueEntityResolver

@AutoMap42
class Bpmn2MessageQueueEntityMapper {
    IBpmn2MessageQueueEntityResolver entityResolver

    @AutoHandler("SC42")
    def void d2eBpmn2MessageQueueDTO(Bpmn2MessageQueueEntity entity, Bpmn2MessageQueueDTO dto, boolean onlyActive) {}
    def void e2dBpmn2MessageQueueDTO(Bpmn2MessageQueueEntity entity, Bpmn2MessageQueueDTO dto) {}

}
