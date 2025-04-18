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
package com.arvatosystems.t9t.base.services.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.arvatosystems.t9t.base.types.ListenerConfig;

/**
 * The cache for JPA listener configuration.
 * The cache is a double nested Map, the first level addressing the classification (one element per JPA entity)
 * and this is never empty for existing entity classes (it is created either when the entity listener is created or
 * when the cache is loaded).
 * The elements are concurrent hash maps.
 * The second level maps tenantIds to the cached configurations.
 * This level is updated with the ListenerConfig CRUD command.
 * The second level may be empty (inactive DTO entries correspond to non existing entries here).
 */
public final class ListenerConfigCache {
    private ListenerConfigCache() { }

    private static final ConcurrentMap<String, ConcurrentMap<String, ListenerConfig>> LISTENER_CONFIG
      = new ConcurrentHashMap<>();

    /** Returns the unique registration map for a given classification.
     * Creates an entry if none exists.
     * Never returns null. Unfortunately the Java 8 implementation is not lock free.
     */
    public static ConcurrentMap<String, ListenerConfig> getRegistrationForClassification(final String classification) {
        return LISTENER_CONFIG.computeIfAbsent(classification, (k) -> new ConcurrentHashMap<>());
    }

    /** Updates an entry or deletes it (if newEntry == null) */
    public static void updateRegistration(final String classification, final String tenantId, final ListenerConfig newEntry) {
        // step 1: get the entry for the given classification
        final ConcurrentMap<String, ListenerConfig> existingMap = getRegistrationForClassification(classification);
        if (newEntry == null)
            existingMap.remove(tenantId);
        else
            existingMap.put(tenantId, newEntry);
    }
}
