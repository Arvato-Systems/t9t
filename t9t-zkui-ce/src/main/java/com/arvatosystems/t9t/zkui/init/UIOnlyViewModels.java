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
package com.arvatosystems.t9t.zkui.init;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepCondition;
import com.arvatosystems.t9t.bpmn.UiOnlyWorkflowStep;
import com.arvatosystems.t9t.monitoring.SystemParamsDTO;

import de.jpaw.bonaparte.pojos.api.TrackingBase;

// pseudo-viewmodels for popups / modal windows
public final class UIOnlyViewModels implements IViewModelContainer {

    private static final CrudViewModel<Info, TrackingBase> INFO_VIEW_MODEL
      = new CrudViewModel<Info, TrackingBase>(Info.BClass.INSTANCE, null, null, null);

    private static final CrudViewModel<UiOnlyWorkflowStep, TrackingBase> WORKFLOW_STEP_CONFIGURATION
      = new CrudViewModel<UiOnlyWorkflowStep, TrackingBase>(UiOnlyWorkflowStep.BClass.INSTANCE, null, null, null);

    private static final CrudViewModel<T9tWorkflowStepCondition, TrackingBase> WORKFLOW_STEP_CONDITION
      = new CrudViewModel<T9tWorkflowStepCondition, TrackingBase>(T9tWorkflowStepCondition.BClass.INSTANCE, null, null, null);

    private static final CrudViewModel<SystemParamsDTO, TrackingBase> SESSION_INFO_VIEW_MODEL =
            new CrudViewModel<SystemParamsDTO, TrackingBase>(
                    SystemParamsDTO.BClass.INSTANCE, null, null, null);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("info",  INFO_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("workflowStepConfig", WORKFLOW_STEP_CONFIGURATION);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("workflowStepCondition", WORKFLOW_STEP_CONDITION);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("sessionInfo",  SESSION_INFO_VIEW_MODEL);
    }
}
