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
package com.arvatosystems.t9t.bpmn.be.services.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.services.IBpmnPersistenceAccess;
import com.arvatosystems.t9t.bpmn.services.IProcessDefinitionCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class ProcessDefinitionCache implements IProcessDefinitionCache {
    private static final Logger LOGGER  = LoggerFactory.getLogger(ProcessDefinitionCache.class);

    protected final Cache<String, ProcessDefinitionDTO> cache = Caffeine.newBuilder().expireAfterWrite(2L,  TimeUnit.MINUTES).build();
    protected final IBpmnPersistenceAccess persistenceAccess = Jdp.getRequired(IBpmnPersistenceAccess.class);
    protected final ICacheInvalidationRegistry registry = Jdp.getRequired(ICacheInvalidationRegistry.class);

    public ProcessDefinitionCache() {
        // register the invalidation callback
        registry.registerInvalidator(ProcessDefinitionDTO.class.getSimpleName(), key -> cache.invalidateAll());
    }

    @Override
    public ProcessDefinitionDTO getCachedProcessDefinitionDTO(final String tenantId, final String processDefinitionId) {
        final String key = tenantId + ":" + processDefinitionId;
        return cache.get(key, unused -> {
            LOGGER.info("Loading cache for ProcessDefinition {} for tenant {}", processDefinitionId, tenantId);
            final ProcessDefinitionDTO dto = persistenceAccess.getProcessDefinitionDTO(processDefinitionId);
            dto.freeze();  // because it's used by many processes, it should not be modifiable any more
            return dto;
        });
    }
}
