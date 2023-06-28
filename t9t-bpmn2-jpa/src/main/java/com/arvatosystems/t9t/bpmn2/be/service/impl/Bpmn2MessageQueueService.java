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
package com.arvatosystems.t9t.bpmn2.be.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.bpmn2.Bpmn2MessageQueueDTO;
import com.arvatosystems.t9t.bpmn2.be.service.IBpmn2MessageQueueService;
import com.arvatosystems.t9t.bpmn2.jpa.entities.Bpmn2MessageQueueEntity;
import com.arvatosystems.t9t.bpmn2.jpa.mapping.IBpmn2MessageQueueDTOMapper;
import com.arvatosystems.t9t.bpmn2.jpa.persistence.IBpmn2MessageQueueEntityResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class Bpmn2MessageQueueService implements IBpmn2MessageQueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bpmn2MessageQueueService.class);

    private final IBpmn2MessageQueueEntityResolver resolver = Jdp.getRequired(IBpmn2MessageQueueEntityResolver.class);
    private final IBpmn2MessageQueueDTOMapper mapper = Jdp.getRequired(IBpmn2MessageQueueDTOMapper.class);

    @Override
    public void queueMessage(final Bpmn2MessageQueueDTO message) {
        final Bpmn2MessageQueueEntity messageEntity = mapper.mapToEntity(message, false);
        resolver.save(messageEntity);

        LOGGER.debug("Created new message queue entry {} with ref {}", message, messageEntity.getObjectRef());
    }

}
