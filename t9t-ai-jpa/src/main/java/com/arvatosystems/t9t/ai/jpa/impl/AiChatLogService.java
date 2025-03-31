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
package com.arvatosystems.t9t.ai.jpa.impl;

import com.arvatosystems.t9t.ai.AiChatLogDTO;
import com.arvatosystems.t9t.ai.jpa.entities.AiChatLogEntity;
import com.arvatosystems.t9t.ai.jpa.mapping.IAiChatLogDTOMapper;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiChatLogEntityResolver;
import com.arvatosystems.t9t.ai.service.IAiChatLogService;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class AiChatLogService implements IAiChatLogService {

    private final IAiChatLogDTOMapper aiChatLogMapper = Jdp.getRequired(IAiChatLogDTOMapper.class);
    private final IAiChatLogEntityResolver aiChatLogResolver = Jdp.getRequired(IAiChatLogEntityResolver.class);

    @Override
    public void saveAiChatLog(final AiChatLogDTO log) {
        final AiChatLogEntity entity = aiChatLogMapper.mapToEntity(log);
        entity.setObjectRef(aiChatLogResolver.createNewPrimaryKey());
        aiChatLogResolver.save(entity);
    }
}
