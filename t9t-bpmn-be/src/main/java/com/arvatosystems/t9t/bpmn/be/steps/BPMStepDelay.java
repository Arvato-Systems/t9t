/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("delay")
public class BPMStepDelay extends AbstractAlwaysRunnableNoFactoryWorkflowStep {
    private static final long TO_MILLISECONDS = 1L;  // value is already in milliseconds

    @Override
    public WorkflowReturnCode execute(Object data, Map<String, Object> parameters) {
        Object yu = parameters.get("yieldUntil");
        if (yu != null) {
            if (Number.class.isAssignableFrom(yu.getClass())) { // Long, Double, BigDecimal...
                // an Instant which has been serialized as JSON and later deserialized will appear as a numeric value, representing the number of (milli)seconds since the Epoch
                yu = new Instant(TO_MILLISECONDS * ((Number)yu).longValue());
                parameters.put("yieldUntil", yu);
            }
            if (yu instanceof Instant) {
                // the target time has been defined
                if (((Instant)yu).isBeforeNow())
                    return WorkflowReturnCode.PROCEED_NEXT;  // limit has been reached
                return WorkflowReturnCode.YIELD;
            }
        }
        // no end time defined yet - compute it now
        Object ds = parameters.get("delayInSeconds");
        if (ds != null && ds instanceof Integer) {
            parameters.put("yieldUntil", Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * (Integer)ds));
            return WorkflowReturnCode.YIELD;
        }
        // missing information!
        parameters.put("returnCode", T9tException.ILLEGAL_REQUEST_PARAMETER);  // set a default error code
        return WorkflowReturnCode.ERROR;
    }
}
