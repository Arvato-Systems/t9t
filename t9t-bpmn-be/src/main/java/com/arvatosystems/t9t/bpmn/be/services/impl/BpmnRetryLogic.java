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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn.services.IBpmnRetryLogic;

import de.jpaw.dp.Singleton;

@Singleton
public class BpmnRetryLogic implements IBpmnRetryLogic {

    @Override
    public WorkflowReturnCode evaluateRetry(final String keyOfRetryCounter, final List<Number> delays, final Map<String, Object> parameters,
      final int errorCode) {
        final Integer retryCounter = JsonUtil.getZInteger(parameters, keyOfRetryCounter, 0);
        if (delays.size() <= retryCounter) {
            // no more retries / delay configurations left
            // report the error, and delete the retry counter (in order to have a full set again in case of additional manual retry)
            parameters.remove(keyOfRetryCounter);
            parameters.put(IWorkflowStep.PROCESS_VARIABLE_RETURN_CODE, errorCode);
            return WorkflowReturnCode.ERROR;
        } else {
            // set the delay as defined per configuration
            final int delayInMinutes = delays.get(retryCounter).intValue();
            parameters.put(IWorkflowStep.PROCESS_VARIABLE_YIELD_UNTIL, Instant.ofEpochMilli(System.currentTimeMillis() + delayInMinutes * 60_000L));
            // store the updated counter
            parameters.put(keyOfRetryCounter, retryCounter + 1);
            return WorkflowReturnCode.YIELD;
        }
    }
}
