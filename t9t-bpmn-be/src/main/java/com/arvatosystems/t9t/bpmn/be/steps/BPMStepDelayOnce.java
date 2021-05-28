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
package com.arvatosystems.t9t.bpmn.be.steps;

import java.util.Map;

import org.joda.time.Instant;

import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/**
 * Delay the workflow for the given number of minutes, but only once time (delete the offset from variables once used).
 */
@Singleton
@Named("delayOnce")
public class BPMStepDelayOnce extends AbstractAlwaysRunnableNoFactoryWorkflowStep {

    @Override
    public WorkflowReturnCode execute(Object data, Map<String, Object> parameters) {
        final Integer waitTime = JsonUtil.getZInteger(parameters, "delayOnceHours", null);
        if (waitTime != null) {
            parameters.remove("delayOnceHours"); // do not use it again
            parameters.put("yieldUntil", Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * 3600L * waitTime));
            return WorkflowReturnCode.YIELD_NEXT;
        }
        final Integer waitTime2 = JsonUtil.getZInteger(parameters, "delayOnceMinutes", null);
        if (waitTime2 != null) {
            parameters.remove("delayOnceMinutes"); // do not use it again
            parameters.put("yieldUntil", Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * 60L * waitTime2));
            return WorkflowReturnCode.YIELD_NEXT;
        }
        final Integer waitTime3 = JsonUtil.getZInteger(parameters, "delayOnceSeconds", null);
        if (waitTime3 != null) {
            parameters.remove("delayOnceSeconds"); // do not use it again
            parameters.put("yieldUntil", Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * waitTime3));
            return WorkflowReturnCode.YIELD_NEXT;
        }
        return WorkflowReturnCode.PROCEED_NEXT;
    }
}
