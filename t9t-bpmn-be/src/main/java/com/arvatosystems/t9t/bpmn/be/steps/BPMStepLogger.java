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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("logger")
public class BPMStepLogger extends AbstractAlwaysRunnableNoFactoryWorkflowStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(BPMStepLogger.class);

    @Override
    public WorkflowReturnCode execute(Object data, Map<String, Object> parameters) {
        Object text = parameters.get("message");
        LOGGER.info(text == null ? "(no text provided)" : text.toString());
        return WorkflowReturnCode.PROCEED_NEXT;
    }
}
