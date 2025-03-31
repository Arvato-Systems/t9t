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
package com.arvatosystems.t9t.bpmn.services;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.plugins.services.PluginMethod;

/**
 * A workflow plugin method extends a regular workflow step implementation directly,
 * because the interface is defined in a suitable way (only defines methods returning basic data types).
 */
public interface IWorkflowStepPlugin<T> extends PluginMethod, IWorkflowStep<T> {

    /** Returns the API implemented. Will usually be provided by a default method. More specific workflow step types are allowed to override this method. */
    @Override
    default String implementsApi() {
        return T9tConstants.PLUGIN_API_ID_WORKFLOW_STEP;
    }
}
