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
package com.arvatosystems.t9t.core.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.core.CannedRequestRef
import com.arvatosystems.t9t.core.jpa.entities.CannedRequestEntity

@AutoResolver42
class CoreResolvers {
//
//    def SubscriberConfigEntity    getSubscriberConfigEntity (SubscriberConfigRef    entityRef, boolean onlyActive) { return null; }
//    def InputConfigEntity getInputConfigEntity (ImportRef  entityRef, boolean onlyActive) { return null; }
//
//    @NoAutomaticTenantFilter
//    def TenantMappings       getTenantMappings   (TenantMappingRef    entityRef, boolean onlyActive) { return null; }

    @AllCanAccessGlobalTenant   // must allow read access to global defaults
    def CannedRequestEntity getCannedRequestEntity(CannedRequestRef  entityRef, boolean onlyActive) { return null; }
}
