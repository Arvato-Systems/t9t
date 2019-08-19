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
package com.arvatosystems.t9t.event.services;

import java.util.Set;

import com.arvatosystems.t9t.base.types.ListenerConfig;
import com.arvatosystems.t9t.event.ListenerConfigDTO;
import com.google.common.collect.ImmutableSet;

public class ListenerConfigConverter {
    public static Set<String> csvToSet(String buckets) {
        if (buckets == null || buckets.length() == 0)
            return null;
        String [] bucketArray = buckets.split(",");
        return ImmutableSet.copyOf(bucketArray);
    }

    public static ListenerConfig convert(ListenerConfigDTO cfg) {
        if (!cfg.getIsActive())
            return null;

        return new ListenerConfig(
            cfg.getIssueCreatedEvents(),
            cfg.getIssueDeletedEvents(),
            cfg.getIssueUpdatedEvents(),
            cfg.getIssueSecondEvents(),
            cfg.getIssueThirdEvents(),
            csvToSet(cfg.getCreationBuckets()),
            csvToSet(cfg.getDeletionBuckets()),
            csvToSet(cfg.getUpdateBuckets()),
            csvToSet(cfg.getSecondBuckets()),
            csvToSet(cfg.getThirdBuckets())
        );
    }
}
