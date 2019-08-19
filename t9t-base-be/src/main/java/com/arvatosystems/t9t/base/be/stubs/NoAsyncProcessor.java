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
package com.arvatosystems.t9t.base.be.stubs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.event.EventData;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.IEventHandler;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Any
@Singleton
public class NoAsyncProcessor implements IAsyncRequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoAsyncProcessor.class);

    public NoAsyncProcessor() {
        LOGGER.warn("NoAsyncProcessor implementation selected - all async commands will be discarded");
    }

    @Override
    public void submitTask(ServiceRequest request) {
        LOGGER.debug("async request {} discarded", request.getRequestParameters().ret$PQON());
    }

    @Override
    public void send(EventData data) {
        LOGGER.debug("async event {} discarded", data.getData().ret$PQON());
    }

    @Override
    public void publish(EventData data) {
        LOGGER.debug("async event {} discarded", data.getData().ret$PQON());
    }

    @Override
    public void registerSubscriber(String eventID, IEventHandler subscriber) {
        registerSubscriber(eventID, T9tConstants.GLOBAL_TENANT_REF42, subscriber);
    }

    @Override
    public void registerSubscriber(String eventID, Long tenantRef, IEventHandler subscriber) {
        LOGGER.debug("subscriber (not) registered: {} for {} of tenant {}", subscriber.getClass().getCanonicalName(), eventID, tenantRef);
    }
}
