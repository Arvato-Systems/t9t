/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.bpmn2.request.EventSubscriptionSearchRequest;
import com.arvatosystems.t9t.bpmn2.request.IncidentSearchRequest;
import com.arvatosystems.t9t.bpmn2.request.ProcessDefinitionSearchRequest;
import com.arvatosystems.t9t.bpmn2.request.ProcessInstanceSearchRequest;

public final class T9tBPMN2Models implements IViewModelContainer {

    private static final CrudViewModel<ProcessDefinitionDTO, FullTrackingWithVersion> PROCESS_DEFINITION_BPMN2_VIEW_MODEL
      = new CrudViewModel<>(
        ProcessDefinitionDTO.BClass.INSTANCE, FullTrackingWithVersion.BClass.INSTANCE,
        ProcessDefinitionSearchRequest.BClass.INSTANCE, null);

    private static final CrudViewModel<ProcessInstanceDTO, FullTrackingWithVersion> PROCESS_INSTANCE_BPMN2_VIEW_MODEL
      = new CrudViewModel<>(
        ProcessInstanceDTO.BClass.INSTANCE, FullTrackingWithVersion.BClass.INSTANCE,
        ProcessInstanceSearchRequest.BClass.INSTANCE, null);

    private static final CrudViewModel<EventSubscriptionDTO, FullTrackingWithVersion> EVENT_SUBSCRIPTIONS_VIEW_MODEL
      = new CrudViewModel<>(
        EventSubscriptionDTO.BClass.INSTANCE, FullTrackingWithVersion.BClass.INSTANCE,
        EventSubscriptionSearchRequest.BClass.INSTANCE, null);

    private static final CrudViewModel<IncidentDTO, FullTrackingWithVersion> INCIDENTS_VIEW_MODEL
      = new CrudViewModel<>(
        IncidentDTO.BClass.INSTANCE, FullTrackingWithVersion.BClass.INSTANCE,
        IncidentSearchRequest.BClass.INSTANCE, null);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("processDefinitionBpmn2", PROCESS_DEFINITION_BPMN2_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("processInstanceBpmn2", PROCESS_INSTANCE_BPMN2_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("eventSubscriptions", EVENT_SUBSCRIPTIONS_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("incidents", INCIDENTS_VIEW_MODEL);
    }
}
