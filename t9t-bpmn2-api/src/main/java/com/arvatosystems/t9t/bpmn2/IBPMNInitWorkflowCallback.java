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

/**
 * <p>
 * Callback interface triggered during process instance startup to initialize process variables.
 * </p>
 *
 * <p>
 * Implementations of this interface are requested from JDP using the workflow type as qualifier. The workflow type is
 * retrieved from the start event property using the key 'WorkflowType'
 * </p>
 *
 * @author TWEL006
 */
public interface IBPMNInitWorkflowCallback {

    /**
     * Initialize process instance variables.
     *
     * @param variables
     *            Modifiable map of process instance variables to initialize. This map also already contains the initial
     *            process instance variables as provided by starting message event.
     * @param properties
     *            Unmodifiable map of workflow parameters as defined on start event activity.
     */
    void init(@Nonnull Map<String, Object> variables, @Nonnull Map<String, String> properties);

    /**
     * <p>
     * Perform lock of refs which need to be locked before execution of workflow is possible.
     * </p>
     *
     * <p>
     * Please note, that this method will be called in two different situations:
     *
     * <ul>
     * <li>During initialization of new process instances <b>before</b> execution of {@link #init(Map, Map)}.</li>
     * <li>During async execution of jobs.</li>
     * </ul>
     *
     * During first situation, not all variables are initialized, thus only the provided variables during message
     * correlation are available. During second situation, there will be all variables available. Thus, following rules
     * need to be followed by this method:
     *
     * <ul>
     * <li>Do not access JPA entity variables, since this would cause them to load before lock is acquired - just access
     * plain ref variables.</li>
     * <li>Since not all refs are available,
     * <ul>
     * <li>either rely only on the data provided by events</li>
     * <li>or (the preferred approach), configure the message start event to be 'async after', which will only perform
     * the {@link #init(Map, Map)} during message correlation and defer anything else to job execution where all refs
     * are available for locking.</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param variables
     *            Unmodifiable map of process instance variables. Depending on the execution context, this map might
     *            only contain the initial variables provided by the message correlation event or all variables as
     *            provided by {@link #init(Map, Map)}.
     */
    void lockRefs(@Nonnull IBPMNReadOnlyContext variables);

}
