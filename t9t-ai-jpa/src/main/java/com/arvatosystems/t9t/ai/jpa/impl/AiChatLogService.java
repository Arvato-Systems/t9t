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
        final AiChatLogEntity entity = aiChatLogMapper.mapToEntity(log, true);
        entity.setObjectRef(aiChatLogResolver.createNewPrimaryKey());
        aiChatLogResolver.save(entity);
    }

}
