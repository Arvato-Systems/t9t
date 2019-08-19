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
package com.arvatosystems.t9t.doc.be.tests

import com.arvatosystems.t9t.doc.DocModuleCfgDTO
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import de.jpaw.dp.Fallback
import de.jpaw.dp.Singleton

@Fallback
@Singleton
class MockedDocModuleCfgDtoResolver implements IDocModuleCfgDtoResolver {

    override getModuleConfiguration() {
        return DEFAULT_MODULE_CFG
    }

    override getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG
    }
    /** Updates module configuration with a new one. Writes to the DB and updates the local cache.
     * Default implementation provided only to simplify creation of mocks.
     * @return
     */
    override updateModuleConfiguration(DocModuleCfgDTO newCfg) {
        throw new UnsupportedOperationException();
    }
}
