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

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Singleton;

/**
 * This class implements a central repository for cache invalidation.
 * For a given key (DTO or JPA entity simple name or PQON), a lambda can be stored which clears the local cache.
 * Usually for configuration data, the whole cache is cleared, ignoring any specific entry.
 * Bigger caches could choose to clear selected entries only.
 *
 * This strategy allows to register a single event listener for all kinds of caches (the map.get here is faster than launching a separate EventListener per cache).
 *
 */
@Singleton
public class CacheInvalidationRegistry implements ICacheInvalidationRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInvalidationRegistry.class);
    private static final ConcurrentHashMap<String, Consumer<BonaPortable>> invalidators = new ConcurrentHashMap<String, Consumer<BonaPortable>>(16);

    @Override
    public void registerInvalidator(String dto, Consumer<BonaPortable> invalidator) {
        LOGGER.info("{} cache invalidator for {}", invalidator == null ? "Deleting" : "Registering", dto);
        if (invalidator == null)
            invalidators.remove(dto);
        else
            invalidators.put(dto, invalidator);
    }

    @Override
    public Consumer<BonaPortable> getInvalidator(String dto) {
        return invalidators.get(dto);
    }
}
