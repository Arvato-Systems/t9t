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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("exception")
public class BPMStepException extends AbstractAlwaysRunnableNoFactoryWorkflowStep {

    @Override
    public WorkflowReturnCode execute(final Object data, final Map<String, Object> parameters) {
        // returnCode and errorDetails should be set in the parameter
        final Object code = parameters.get(PROCESS_VARIABLE_RETURN_CODE);
        final Object errorDetails = parameters.get(PROCESS_VARIABLE_RETURN_CODE);
        final String details = errorDetails == null ? null : errorDetails.toString();
        if (code == null || !(code instanceof Integer))
            throw new T9tException(T9tException.UNSUPPORTED_OPERATION, details);
        throw new T9tException((Integer)code, details);
    }
}
