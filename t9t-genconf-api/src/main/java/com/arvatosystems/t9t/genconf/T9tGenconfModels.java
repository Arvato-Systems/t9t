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
package com.arvatosystems.t9t.genconf;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.genconf.request.ConfigCrudRequest;
import com.arvatosystems.t9t.genconf.request.ConfigSearchRequest;
import com.arvatosystems.t9t.genconf.request.GenericConfigCrudRequest;
import com.arvatosystems.t9t.genconf.request.GenericConfigSearchRequest;

public final class T9tGenconfModels implements IViewModelContainer {

    private static final CrudViewModel<ConfigDTO, FullTrackingWithVersion> CONFIG_VIEW_MODEL
      = new CrudViewModel<>(
        ConfigDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ConfigSearchRequest.BClass.INSTANCE,
        ConfigCrudRequest.BClass.INSTANCE);

    private static final CrudViewModel<GenericConfigDTO, FullTrackingWithVersion> GENERIC_CONFIG_V2_VIEW_MODEL
            = new CrudViewModel<>(
            GenericConfigDTO.BClass.INSTANCE,
            FullTrackingWithVersion.BClass.INSTANCE,
            GenericConfigSearchRequest.BClass.INSTANCE,
            GenericConfigCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("genericConfig", CONFIG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("genericConfigV2", GENERIC_CONFIG_V2_VIEW_MODEL);
    }
}
