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
package com.arvatosystems.t9t.uiprefs;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigDTO;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigCrudRequest;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigSearchRequest;

public final class T9tUiprefsModels implements IViewModelContainer {

    private static final CrudViewModel<LeanGridConfigDTO, FullTrackingWithVersion> LEAN_GRID_CONFIG_VIEW_MODEL
      = new CrudViewModel<>(
        LeanGridConfigDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        LeanGridConfigSearchRequest.BClass.INSTANCE,
        LeanGridConfigCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("leanGridConfig",        LEAN_GRID_CONFIG_VIEW_MODEL);
    }
}
