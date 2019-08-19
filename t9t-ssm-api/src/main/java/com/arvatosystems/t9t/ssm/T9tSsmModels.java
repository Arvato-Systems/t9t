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
package com.arvatosystems.t9t.ssm;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest;
import com.arvatosystems.t9t.ssm.request.SchedulerSetupSearchRequest;

public class T9tSsmModels implements IViewModelContainer {
    public static final CrudViewModel<SchedulerSetupDTO, FullTrackingWithVersion> SCHEDULER_SETUP_VIEW_MODEL = new CrudViewModel<SchedulerSetupDTO, FullTrackingWithVersion>(
            SchedulerSetupDTO.BClass.INSTANCE,
            FullTrackingWithVersion.BClass.INSTANCE,
            SchedulerSetupSearchRequest.BClass.INSTANCE,
            SchedulerSetupCrudRequest.BClass.INSTANCE);
    static {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("schedulerSetup",  SCHEDULER_SETUP_VIEW_MODEL);
    }
}
