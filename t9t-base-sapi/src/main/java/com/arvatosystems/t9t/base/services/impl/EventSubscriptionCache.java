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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;

/** Class stores map of active subscriptions in memory.
 * We often want to know if there is a subscription to tenant + qualifier.
 */
public final class EventSubscriptionCache {
    private EventSubscriptionCache() { }

    private static final ConcurrentMap<String, Boolean> ACTIVE_SUBSCRIPTIONS = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(EventSubscriptionCache.class);

    public static void updateRegistration(final String eventID, final String qualifier, final String tenantId, final boolean active) {
        final String key = eventID + ":" + qualifier + ":" + tenantId;
        ACTIVE_SUBSCRIPTIONS.put(key, Boolean.valueOf(active));
        LOGGER.info("Updated EventSubscriptionCache with key:{}, active:{}", key, active);
    }

    public static boolean isActive(final String eventID, final String qualifier, final String tenantId) {
        final String key = eventID + ":" + qualifier + ":" + tenantId;
        return T9tUtil.isTrue(ACTIVE_SUBSCRIPTIONS.get(key));
    }
}
