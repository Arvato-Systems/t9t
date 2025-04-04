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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import java.time.Instant;

import com.arvatosystems.t9t.base.entities.FullTracking;

/**
 * Utility methods for usage to bridge T9T search requests with Camunda query API.
 *
 * @author TWEL006
 */
public abstract class SearchRequestUtils {

    /**
     * Since base tracking field are not all available but required by T9T, initialize with default values.
     */
    public static void initTrackingFields(FullTracking tracking) {
        tracking.setCAppUserId("BPMN ENGINE");
        tracking.setCProcessRef(-1L);
        tracking.setCTimestamp(Instant.now());

        tracking.setMAppUserId("BPMN ENGINE");
        tracking.setMProcessRef(-1L);
        tracking.setMTimestamp(Instant.now());
    }
}
