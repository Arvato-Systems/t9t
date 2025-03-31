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
package com.arvatosystems.t9t.bpmn.be.steps;

import java.time.Instant;
import java.util.Map;

import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("yieldNextForever")
public class BPMStepYieldNextForever extends AbstractAlwaysRunnableNoFactoryWorkflowStep {
    public static final Instant FAR_FUTURE = Instant.ofEpochSecond(3_2503_676_400L); // Wed Jan 01 3000 00:00:00 GMT+0100

    @Override
    public WorkflowReturnCode execute(final Object data, final Map<String, Object> parameters) {
        parameters.put(PROCESS_VARIABLE_YIELD_UNTIL, FAR_FUTURE);
        return WorkflowReturnCode.YIELD_NEXT;
    }
}
