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
package com.arvatosystems.t9t.ssm.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.RandomNumberGenerators;
import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.ssm.event.SchedulerChangedEvent;
import com.arvatosystems.t9t.ssm.services.ISchedulerService;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/**
 * Event to be executed on other nodes if a scheduler change has been performed.
 */
@Singleton
@Named("SsmSchedulerChange")
public class SchedulerUpdateListener implements IEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerUpdateListener.class);

    private final ISchedulerService schedulerService = Jdp.getRequired(ISchedulerService.class);

    @Override
    public int execute(final RequestContext context, final EventParameters untypedEvent) {
        if (untypedEvent instanceof SchedulerChangedEvent event) {
            if (!RandomNumberGenerators.THIS_JVM_ID.equals(event.getSenderJvmId())) {
                LOGGER.info("Received scheduler update event");
                schedulerService.updateScheduler(context, event.getOperationType(), event.getSchedulerId(), event.getSetup());
            } // else silently discard: this has been updated directly
        } else {
            LOGGER.error("Discarding event of wrong type {}", untypedEvent == null ? "(NULL)" : untypedEvent.getClass().getCanonicalName());
        }
        return 0;
    }
}
