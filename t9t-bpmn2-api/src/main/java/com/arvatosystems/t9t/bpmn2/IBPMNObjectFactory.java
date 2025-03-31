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
package com.arvatosystems.t9t.bpmn2;

import java.util.Map;

import javax.annotation.Nonnull;

import com.arvatosystems.t9t.bpmn.IWorkflowStep;

/**
 * <p>
 * Factory to create the workflow data object using the given process instance variables.
 * </p>
 *
 * <p>
 * The workflow data object created by this factory is passed to the {@link IWorkflowStep} during execution. Instances
 * of this factory are requested from JDP using the {@link IWorkflowStep#getFactoryName()} as qualifier.
 * </p>
 *
 * @author TWEL006
 *
 * @param <T>
 *            Type of workflow data object created
 */
public interface IBPMNObjectFactory<T> {

    /**
     * Create an instance of the workflow data object using given process instance variables.
     *
     * @param variables
     *            Modifiable map of process instance variables.
     * @return Workflow data object to use.
     */
    T create(@Nonnull Map<String, Object> variables);

}
