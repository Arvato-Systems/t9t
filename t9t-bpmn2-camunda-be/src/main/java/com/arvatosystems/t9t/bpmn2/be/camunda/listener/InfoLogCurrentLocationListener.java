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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.bpmn2.be.camunda.utils.MDCHelper;

import de.jpaw.dp.Singleton;

@Singleton
public class InfoLogCurrentLocationListener implements ExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoLogCurrentLocationListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }

        try (AutoCloseable mdc = MDCHelper.put(execution)) {
            if (EVENTNAME_TAKE.equals(execution.getEventName())) {
                LOGGER.info("transition {}", execution.getCurrentTransitionId());
            } else {
                LOGGER.info("{} activity '{}'", execution.getEventName(), execution.getCurrentActivityName());
            }
        }
    }

}
