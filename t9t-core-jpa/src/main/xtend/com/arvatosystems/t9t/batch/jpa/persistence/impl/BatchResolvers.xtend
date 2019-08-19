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
package com.arvatosystems.t9t.batch.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.batch.SliceTrackingRef
import com.arvatosystems.t9t.batch.StatisticsRef
import com.arvatosystems.t9t.batch.jpa.entities.SliceTrackingEntity
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsEntity

@AutoResolver42
class BatchResolvers {
    def StatisticsEntity        getStatisticsEntity   (StatisticsRef    entityRef, boolean onlyActive) { return null; }
    def SliceTrackingEntity     getSliceTrackingEntity(SliceTrackingRef entityRef, boolean onlyActive) { return null; }
    def SliceTrackingEntity     findByDataSinkIdAndId(boolean onlyActive, String dataSinkId, String id) {null}
}
