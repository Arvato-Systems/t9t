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

import com.arvatosystems.t9t.ai.AiModuleCfgDTO;
import com.arvatosystems.t9t.ai.jpa.entities.AiModuleCfgEntity;
import com.arvatosystems.t9t.ai.jpa.persistence.IAiModuleCfgEntityResolver;
import com.arvatosystems.t9t.ai.service.IAiModuleCfgDtoResolver;
import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver;

import de.jpaw.dp.Singleton;

@Singleton
public class AiModuleCfgDtoResolver extends AbstractModuleConfigResolver<AiModuleCfgDTO, AiModuleCfgEntity> implements IAiModuleCfgDtoResolver {

    private static final AiModuleCfgDTO DEFAULT_MODULE_CFG = new AiModuleCfgDTO(
            null       // Json z
        );

    public AiModuleCfgDtoResolver() {
        super(IAiModuleCfgEntityResolver.class);
    }

    @Override
    public AiModuleCfgDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
