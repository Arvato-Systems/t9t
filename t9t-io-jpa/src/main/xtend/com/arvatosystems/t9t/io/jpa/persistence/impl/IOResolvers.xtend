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
package com.arvatosystems.t9t.io.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.annotations.jpa.GlobalTenantCanAccessAll
import com.arvatosystems.t9t.io.AsyncChannelRef
import com.arvatosystems.t9t.io.AsyncMessageRef
import com.arvatosystems.t9t.io.CsvConfigurationRef
import com.arvatosystems.t9t.io.DataSinkRef
import com.arvatosystems.t9t.io.OutboundMessageRef
import com.arvatosystems.t9t.io.SinkRef
import com.arvatosystems.t9t.io.jpa.entities.AsyncChannelEntity
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity
import com.arvatosystems.t9t.io.jpa.entities.CsvConfigurationEntity
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity
import com.arvatosystems.t9t.io.jpa.entities.OutboundMessageEntity
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity
import java.util.List
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity
import com.arvatosystems.t9t.io.AsyncQueueRef

@AutoResolver42
class IOResolvers {
    @GlobalTenantCanAccessAll   // required for Camel startup
    @AllCanAccessGlobalTenant   // must allow read access to global defaults
    def CsvConfigurationEntity       getCsvConfigurationEntity          (CsvConfigurationRef entityRef, boolean onlyActive) { return null; }
    def List<CsvConfigurationEntity> findByCsvConfigurationIdWithDefault(boolean onlyActive, String csvConfigurationId)     { return null; }

    @GlobalTenantCanAccessAll   // required for Camel startup
    @AllCanAccessGlobalTenant   // for DataSinkEntity, everyone can see the global tenant's defaults
    def DataSinkEntity          getDataSinkEntity           (DataSinkRef  entityRef, boolean onlyActive) { return null; }
    def List<DataSinkEntity>    findByDataSinkIdWithDefault (boolean onlyActive, String dataSinkId) { return null; }

    def SinkEntity              getSinkEntity               (SinkRef      entityRef, boolean onlyActive) { return null; }

    def OutboundMessageEntity   getOutboundMessageEntity    (OutboundMessageRef entityRef, boolean onlyActive) { return null; }
    def AsyncMessageEntity      getAsyncMessageEntity       (AsyncMessageRef entityRef, boolean onlyActive) { return null; }
    @GlobalTenantCanAccessAll   // required for Camel startup
    @AllCanAccessGlobalTenant   // for DataSinkEntity, everyone can see the global tenant's defaults
    def AsyncChannelEntity      getAsyncChannelEntity       (AsyncChannelRef entityRef, boolean onlyActive) { return null; }
    @GlobalTenantCanAccessAll   // required for Camel startup
    @AllCanAccessGlobalTenant   // for DataSinkEntity, everyone can see the global tenant's defaults
    def AsyncQueueEntity        getAsyncQueueEntity         (AsyncQueueRef entityRef, boolean onlyActive) { return null; }
}
