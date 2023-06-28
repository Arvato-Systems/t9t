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
package com.arvatosystems.t9t.rep;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.rep.request.ReportConfigCrudRequest;
import com.arvatosystems.t9t.rep.request.ReportConfigSearchRequest;
import com.arvatosystems.t9t.rep.request.ReportParamsCrudRequest;
import com.arvatosystems.t9t.rep.request.ReportParamsSearchRequest;

public final class T9tRepModels implements IViewModelContainer {

    private static final CrudViewModel<ReportConfigDTO, FullTrackingWithVersion> REPORT_CONFIG_VIEW_MODEL
      = new CrudViewModel<>(
        ReportConfigDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ReportConfigSearchRequest.BClass.INSTANCE,
        ReportConfigCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<ReportParamsDTO, FullTrackingWithVersion> REPORT_PARAMS_VIEW_MODEL
      = new CrudViewModel<>(
        ReportParamsDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ReportParamsSearchRequest.BClass.INSTANCE,
        ReportParamsCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("reportConfig",    REPORT_CONFIG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("reportParams",    REPORT_PARAMS_VIEW_MODEL);
    }
}
