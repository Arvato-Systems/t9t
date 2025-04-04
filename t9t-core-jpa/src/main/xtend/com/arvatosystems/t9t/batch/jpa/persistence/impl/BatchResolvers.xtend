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
package com.arvatosystems.t9t.batch.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import com.arvatosystems.t9t.batch.RecordEventsRef
import com.arvatosystems.t9t.batch.SliceTrackingRef
import com.arvatosystems.t9t.batch.StatisticsAggregationRef
import com.arvatosystems.t9t.batch.StatisticsRef
import com.arvatosystems.t9t.batch.jpa.entities.RecordEventsEntity
import com.arvatosystems.t9t.batch.jpa.entities.SliceTrackingEntity
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsAggregationEntity
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsEntity

@AutoResolver42
class BatchResolvers {
    def StatisticsEntity        getStatisticsEntity   (StatisticsRef    entityRef) { return null; }
    def StatisticsAggregationEntity getStatisticsAggregationEntity (StatisticsAggregationRef entityRef) { return null; }
    def SliceTrackingEntity     getSliceTrackingEntity(SliceTrackingRef entityRef) { return null; }
    def SliceTrackingEntity     findByDataSinkIdAndId(boolean onlyActive, String dataSinkId, String id) {null}
    def RecordEventsEntity        getRecordEventsEntity(RecordEventsRef    entityRef) { return null; }
}
