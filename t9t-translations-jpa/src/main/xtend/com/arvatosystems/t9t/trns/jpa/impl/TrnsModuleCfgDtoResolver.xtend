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
package com.arvatosystems.t9t.trns.jpa.impl

import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver
import com.arvatosystems.t9t.trns.TrnsModuleCfgDTO
import com.arvatosystems.t9t.trns.jpa.entities.TrnsModuleCfgEntity
import com.arvatosystems.t9t.trns.jpa.persistence.ITrnsModuleCfgEntityResolver
import com.arvatosystems.t9t.trns.services.ITrnsModuleCfgDtoResolver
import de.jpaw.dp.Singleton

@Singleton
class TrnsModuleCfgDtoResolver extends AbstractModuleConfigResolver<TrnsModuleCfgDTO, TrnsModuleCfgEntity> implements ITrnsModuleCfgDtoResolver {
    private static final TrnsModuleCfgDTO DEFAULT_MODULE_CFG = new TrnsModuleCfgDTO(
        null,       // Json z
        false,      // attemptLocalTenant
        true        // attemptDialects
    )

    public new() {
        super(ITrnsModuleCfgEntityResolver)
    }

    override public TrnsModuleCfgDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
