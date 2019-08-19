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
package com.arvatosystems.t9t.msglog.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.annotations.jpa.GlobalTenantCanAccessAll
import com.arvatosystems.t9t.msglog.MessageRef
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity

@AutoResolver42
class MsglogResolvers {
//
//    def InputConfigEntity getInputConfigEntity (ImportRef  entityRef, boolean onlyActive) { return null; }
//
//    @NoAutomaticTenantFilter
//    def TenantMappings       getTenantMappings   (TenantMappingRef    entityRef, boolean onlyActive) { return null; }
//
//    def StatisticsEntity     getStatisticsEntity (StatisticsDataRef   entityRef, boolean onlyActive) { return null; }
//    def SliceTrackingEntity     getSliceTrackingEntity (SliceTrackingDataRef   entityRef, boolean onlyActive) { return null; }
//
//    @TenantFilterMeOrGlobal  // for DataSinkEntity, everyone can see the global tenant's defaults
//    def GridConfigEntity   getGridConfigEntity (DefaultGridConfigurationRef  entityRef, boolean onlyActive) { return null; }
//    def List<GridConfigEntity> findByGridIdNLanguageWithDefault(boolean onlyActive, String gridId, String language) { return null; }
//
//
//    def SubscriberConfigEntity    getSubscriberConfigEntity (SubscriberConfigRef    entityRef, boolean onlyActive) { return null; }

    @GlobalTenantCanAccessAll   // admin access to all tenant's data
    def MessageEntity       getMessageEntity(MessageRef     entityRef, boolean onlyActive) { return null; }
}
