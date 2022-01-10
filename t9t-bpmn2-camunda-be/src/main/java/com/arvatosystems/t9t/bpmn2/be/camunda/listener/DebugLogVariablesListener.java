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
package com.arvatosystems.t9t.bpmn2.be.camunda.listener;

import java.util.Map.Entry;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.bpmn2.be.camunda.utils.MDCHelper;

import de.jpaw.dp.Singleton;

/**
 * Listener which can be added to process definition to provide more information on debug level:
 * If triggered, the current variables are provided on debug log.
 */
@Singleton
public class DebugLogVariablesListener implements ExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugLogVariablesListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }

        try (AutoCloseable mdc = MDCHelper.put(execution)) {
            if (EVENTNAME_TAKE.equals(execution.getEventName())) {
                LOGGER.debug("Current variables (event {} on transition {})", execution.getEventName(), execution.getCurrentTransitionId());
            } else {
                LOGGER.debug("Current variables (event {})", execution.getEventName());
            }

            for (Entry<String, Object> e : execution.getVariables()
                                                    .entrySet()) {
                LOGGER.debug(" {}={}", e.getKey(), e.getValue());
            }
        }
    }

}
