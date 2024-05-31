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
package com.arvatosystems.t9t.bpmn.be.steps;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("delay")
public class BPMStepDelay extends AbstractAlwaysRunnableNoFactoryWorkflowStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(BPMStepDelay.class);

    @Override
    public WorkflowReturnCode execute(final Object data, final Map<String, Object> parameters) {
        // compute end time
        final Object ds = parameters.get("delayInSeconds");
        if (ds instanceof Number dsNumber) {
            // obtain a value rounded to full seconds
            parameters.put(PROCESS_VARIABLE_YIELD_UNTIL, Instant.ofEpochMilli((System.currentTimeMillis() / 1000L + dsNumber.longValue()) * 1000L));
            return WorkflowReturnCode.YIELD_NEXT;
        }
        // missing information!
        LOGGER.error("Process variable {} not defined or not a number: {}", "delayInSeconds", ds == null ? "null" : ds.getClass().getName());
        parameters.put(PROCESS_VARIABLE_RETURN_CODE, T9tException.ILLEGAL_REQUEST_PARAMETER);  // set a default error code
        return WorkflowReturnCode.ERROR;
    }
}
