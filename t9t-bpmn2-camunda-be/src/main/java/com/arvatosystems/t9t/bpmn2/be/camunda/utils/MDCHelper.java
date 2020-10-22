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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.T9tConstants;

public abstract class MDCHelper {

    public static AutoCloseable put(DelegateExecution execution) {
        MDC.put(T9tConstants.MDC_BPMN_STEP, execution.getCurrentActivityId());
        MDC.put(T9tConstants.MDC_BPMN_PROCESS, execution.getProcessDefinitionId());
        MDC.put(T9tConstants.MDC_BPMN_PROCESS_INSTANCE, execution.getProcessInstanceId());

        return () -> {
            MDC.remove(T9tConstants.MDC_BPMN_STEP);
            MDC.remove(T9tConstants.MDC_BPMN_PROCESS);
            MDC.remove(T9tConstants.MDC_BPMN_PROCESS_INSTANCE);
        };
    }

}
