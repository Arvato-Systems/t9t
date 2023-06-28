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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import org.camunda.bpm.engine.RuntimeService;

import com.arvatosystems.t9t.bpmn2.IBPMNReadOnlyContext;

public class BPMNExecutionReadOnlyContext implements IBPMNReadOnlyContext {

    private final RuntimeService runtimeService;
    private final String executionId;

    public BPMNExecutionReadOnlyContext(RuntimeService runtimeService, String executionId) {
        this.runtimeService = runtimeService;
        this.executionId = executionId;
    }

    @Override
    public Object getVariable(String variableName) {
        return runtimeService.getVariable(executionId, variableName);
    }

}
