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
package com.arvatosystems.t9t.base;

import com.arvatosystems.t9t.base.auth.ChangePasswordUI;
import com.arvatosystems.t9t.base.output.ExportParameters;

import de.jpaw.bonaparte.pojos.api.TrackingBase;

public final class T9tBaseModels implements IViewModelContainer {

    private static final CrudViewModel<ExportParameters, TrackingBase> EXPORT_PARAMS_VIEW_MODEL
      = new CrudViewModel<>(ExportParameters.BClass.INSTANCE, null, null, null);

    private static final CrudViewModel<ChangePasswordUI, TrackingBase> CHANGE_PWD_VIEW_MODEL
      = new CrudViewModel<>(ChangePasswordUI.BClass.INSTANCE, null, null, null);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("exportParams",  EXPORT_PARAMS_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("changePwd",  CHANGE_PWD_VIEW_MODEL);
    }
}
