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
package com.arvatosystems.t9t.components.crud;

import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.base.crud.CrudAnyKeyResponse;
import com.arvatosystems.t9t.base.crud.CrudModuleCfgRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigDTO;
import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigKey;

@Init(superclass=true)
public class ModuleConfigVM<DTO extends ModuleConfigDTO>
extends AbstractCrudVM<ModuleConfigKey, DTO, FullTrackingWithVersion, CrudModuleCfgRequest<DTO>, CrudAnyKeyResponse<DTO, FullTrackingWithVersion>> {
    private static final ModuleConfigKey KEY = new ModuleConfigKey();
    static {
        KEY.freeze();
    }

    @Override
    protected CrudModuleCfgRequest<DTO> createCrudWithKey() {
        CrudModuleCfgRequest<DTO> crudRq = (CrudModuleCfgRequest<DTO>) crudViewModel.crudClass.newInstance();
        crudRq.setKey(KEY);
        return crudRq;
    }

    @Override
    protected void clearKey() {
    }
}
