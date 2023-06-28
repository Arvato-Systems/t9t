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
package com.arvatosystems.t9t.trns;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.trns.request.TranslationsCrudRequest;
import com.arvatosystems.t9t.trns.request.TranslationsSearchRequest;
import com.arvatosystems.t9t.trns.request.TrnsModuleCfgCrudRequest;
import com.arvatosystems.t9t.trns.request.TrnsModuleCfgSearchRequest;

public final class TranslationsModels implements IViewModelContainer {

    private static final CrudViewModel<TranslationsDTO, FullTrackingWithVersion> TRANSLATIONS_VIEW_MODEL
      = new CrudViewModel<>(
          TranslationsDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        TranslationsSearchRequest.BClass.INSTANCE,
        TranslationsCrudRequest.BClass.INSTANCE);

    private static final CrudViewModel<TrnsModuleCfgDTO, FullTrackingWithVersion> TRNS_MODULE_CFG_VIEW_MODEL
      = new CrudViewModel<>(
        TrnsModuleCfgDTO.BClass.INSTANCE,
      FullTrackingWithVersion.BClass.INSTANCE,
      TrnsModuleCfgSearchRequest.BClass.INSTANCE,
      TrnsModuleCfgCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("translations", TRANSLATIONS_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("trnsModuleCfg", TRNS_MODULE_CFG_VIEW_MODEL);
    }
}
