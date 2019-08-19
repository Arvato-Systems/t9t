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
package com.arvatosystems.t9t.doc.services;

import com.arvatosystems.t9t.doc.DocModuleCfgDTO;
import com.arvatosystems.t9t.server.services.IModuleConfigResolver;

public interface IDocModuleCfgDtoResolver extends IModuleConfigResolver<DocModuleCfgDTO> {
    public static final DocModuleCfgDTO DEFAULT_MODULE_CFG = new DocModuleCfgDTO(
        null,       // Json z
        true,       // considerGlobalTemplates
        true,       // considerGlobalTexts
        true,       // considerGlobalBinaries
        1_000_000,  // weightTenantMatch
        10_000,     // weightLanguageMatch
        1,          // weightCurrencyMatch
        100,        // weightCountryMatch
        1000        // weightEntityMatch
    );
}
