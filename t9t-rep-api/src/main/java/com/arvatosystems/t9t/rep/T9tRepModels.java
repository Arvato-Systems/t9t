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
package com.arvatosystems.t9t.rep;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.rep.request.ReportConfigCrudRequest;
import com.arvatosystems.t9t.rep.request.ReportConfigSearchRequest;
import com.arvatosystems.t9t.rep.request.ReportMailingCrudRequest;
import com.arvatosystems.t9t.rep.request.ReportMailingSearchRequest;
import com.arvatosystems.t9t.rep.request.ReportParamsCrudRequest;
import com.arvatosystems.t9t.rep.request.ReportParamsSearchRequest;

public class T9tRepModels implements IViewModelContainer {
    public static final CrudViewModel<ReportConfigDTO, FullTrackingWithVersion> REPORT_CONFIG_VIEW_MODEL = new CrudViewModel<ReportConfigDTO, FullTrackingWithVersion>(
        ReportConfigDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ReportConfigSearchRequest.BClass.INSTANCE,
        ReportConfigCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<ReportParamsDTO, FullTrackingWithVersion> REPORT_PARAMS_VIEW_MODEL = new CrudViewModel<ReportParamsDTO, FullTrackingWithVersion>(
        ReportParamsDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ReportParamsSearchRequest.BClass.INSTANCE,
        ReportParamsCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<ReportMailingDTO, FullTrackingWithVersion> REPORT_MAILING_VIEW_MODEL = new CrudViewModel<ReportMailingDTO, FullTrackingWithVersion>(
            ReportMailingDTO.BClass.INSTANCE,
            FullTrackingWithVersion.BClass.INSTANCE,
            ReportMailingSearchRequest.BClass.INSTANCE,
            ReportMailingCrudRequest.BClass.INSTANCE);

    static {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("reportConfig",    REPORT_CONFIG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("reportParams",    REPORT_PARAMS_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("reportMailing",   REPORT_MAILING_VIEW_MODEL);
    }
}
