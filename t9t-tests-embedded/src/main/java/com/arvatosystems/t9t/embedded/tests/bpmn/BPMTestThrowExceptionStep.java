/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.embedded.tests.bpmn;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn.be.steps.AbstractAlwaysRunnableNoFactoryWorkflowStep;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/**
 * Workflow step for testing purpose. It just throws an exception.
 */
@Singleton
@Named("throwException")
public class BPMTestThrowExceptionStep extends AbstractAlwaysRunnableNoFactoryWorkflowStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(BPMTestThrowExceptionStep.class);

    public static final String STEP_NAME = "throwException";
    public static final String ERROR_DETAILS = "Some details";
    public static final int ERROR_CODE = T9tException.GENERAL_EXCEPTION;

    @Override
    public WorkflowReturnCode execute(final Object data, final Map<String, Object> parameters) {
        LOGGER.info("Executing throwException...");
        throw new T9tException(ERROR_CODE, ERROR_DETAILS);
    }
}
