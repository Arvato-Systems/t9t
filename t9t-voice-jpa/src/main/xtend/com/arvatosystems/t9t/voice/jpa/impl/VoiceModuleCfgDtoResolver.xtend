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
package com.arvatosystems.t9t.voice.jpa.impl

import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver
import com.arvatosystems.t9t.voice.VoiceModuleCfgDTO
import com.arvatosystems.t9t.voice.jpa.entities.VoiceModuleCfgEntity
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceModuleCfgEntityResolver
import com.arvatosystems.t9t.voice.services.IVoiceModuleCfgDtoResolver
import de.jpaw.dp.Singleton

@Singleton
class VoiceModuleCfgDtoResolver extends AbstractModuleConfigResolver<VoiceModuleCfgDTO, VoiceModuleCfgEntity> implements IVoiceModuleCfgDtoResolver {

    public new() {
        super(IVoiceModuleCfgEntityResolver)
    }

    override public VoiceModuleCfgDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
