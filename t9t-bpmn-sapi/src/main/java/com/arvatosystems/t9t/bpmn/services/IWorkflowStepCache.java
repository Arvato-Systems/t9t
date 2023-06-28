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
package com.arvatosystems.t9t.bpmn.services;

import com.arvatosystems.t9t.bpmn.IBPMObjectFactory;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;

public interface IWorkflowStepCache {
    /** Initializes the cache with all statically defined workflow steps. Invoked by @Startup(50080). */
    void loadCaches();

    /** Add an additional workflow step instance at runtime. This is also registered via Jdp. */
    void addToCache(IWorkflowStep<?> step, String name);

    IWorkflowStep<?> getWorkflowStepForName(String name);
    IBPMObjectFactory<?> getBPMObjectFactoryForName(String name);

//    /** Returns an immutable map of all eligible steps. */
//    Map<String,IWorkflowStep> getAllSteps();
//
//    /** Returns an immutable map of all eligible factories. */
//    Map<String,IBPMObjectFactory> getAllFactories();
}
