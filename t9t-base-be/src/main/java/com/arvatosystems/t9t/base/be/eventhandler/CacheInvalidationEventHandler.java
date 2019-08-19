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
package com.arvatosystems.t9t.base.be.eventhandler;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.event.InvalidateCacheEvent;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("cacheInvalidation")
public class CacheInvalidationEventHandler implements IEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInvalidationEventHandler.class);
    protected final ICacheInvalidationRegistry registry = Jdp.getRequired(ICacheInvalidationRegistry.class);

    @Override
    public int execute(RequestContext ctx, EventParameters eventData) {
        if (eventData instanceof InvalidateCacheEvent) {
            // OK, query registry and invoke invalidator, if any exists
            InvalidateCacheEvent ice = (InvalidateCacheEvent) eventData;
            Consumer<BonaPortable> invalidator = registry.getInvalidator(ice.getPqon());
            if (invalidator != null) {
                LOGGER.debug("Performing cache invalidation for {} with key {}", ice.getPqon(), ice.getKey());
                invalidator.accept(ice.getKey());
            } else {
                LOGGER.debug("Ignoring cache invalidation event for {} - no invalidator registered", ice.getPqon());
            }
        } else {
            LOGGER.warn("Event data is of type {}, expected InvalidateCacheEvent", eventData.getClass().getCanonicalName());
        }
        return 0;
    }
}
