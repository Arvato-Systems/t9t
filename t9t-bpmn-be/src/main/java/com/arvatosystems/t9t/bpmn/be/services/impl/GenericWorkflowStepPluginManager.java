/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn.be.services.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named(T9tConstants.PLUGIN_API_ID_WORKFLOW_STEP)
public class GenericWorkflowStepPluginManager<T> extends AbstractWorkflowStepPluginManager<T, IWorkflowStep<T>, GenericWorkflowStepWithPlugin<T>> {

    @Override
    public Class<IWorkflowStep<T>> getCompatibleSuperclass() {
        return (Class)IWorkflowStep.class;
    }

    @Override
    public Class<GenericWorkflowStepWithPlugin<T>> getPluginWrapperClass(final IWorkflowStep<T> pluginInstance) {
        return (Class)GenericWorkflowStepWithPlugin.class;
    }

    @Override
    public GenericWorkflowStepWithPlugin<T> createWrapper(final String qualifier, final IWorkflowStep<T> existingInstance) {
        return new GenericWorkflowStepWithPlugin(qualifier, existingInstance);
    }
}
