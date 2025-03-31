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
package com.arvatosystems.t9t.bpmn.be.services.impl;

import java.util.Map;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn.WorkflowRunnableCode;
import com.arvatosystems.t9t.bpmn.services.IWorkflowStepPlugin;
import com.arvatosystems.t9t.plugins.services.IPluginManager;

import de.jpaw.dp.Jdp;

/**
 * Instances of class act as a proxy / dispatcher to plugin workflow methods. There is one instance created per qualifier.
 *
 * @param <T> the type of workflow data
 */
public class GenericWorkflowStepWithPlugin<T> implements IWorkflowStep<T> {
    private final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    public final String qualifier;
    private final IWorkflowStep<T> staticStep;

    public GenericWorkflowStepWithPlugin(final String qualifier, final IWorkflowStep<T> staticStep) {
        this.qualifier = qualifier;
        this.staticStep = staticStep;
    }

    /** returns the relevant implementation of the plugin, or the static step (by tenantId) or throw an exception, if there is none. */
    private IWorkflowStep<T> getImplementation() {
        final IWorkflowStepPlugin<T> plugin
          = pluginManager.getPluginMethod(T9tConstants.PLUGIN_API_ID_WORKFLOW_STEP, qualifier, IWorkflowStepPlugin.class, staticStep != null);
        return plugin != null ? plugin : staticStep;
    }

    @Override
    public String getFactoryName() {
        return getImplementation().getFactoryName();
    }

    @Override
    public WorkflowReturnCode execute(final T data, final Map<String, Object> parameters) {
        return getImplementation().execute(data, parameters);
    }

    @Override
    public WorkflowRunnableCode mayRun(final T data, final Map<String, Object> parameters) {
        return getImplementation().mayRun(data, parameters);
    }
}
