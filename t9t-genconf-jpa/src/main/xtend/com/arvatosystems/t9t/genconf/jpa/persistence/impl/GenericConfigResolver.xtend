/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.genconf.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import com.arvatosystems.t9t.genconf.GenericConfigRef
import com.arvatosystems.t9t.genconf.jpa.entities.GenericConfigEntity
import java.util.List

@AutoResolver42
class GenericConfigResolver {

    @AllCanAccessGlobalTenant  // for DataSinkEntity, everyone can see the global tenant's defaults
    def GenericConfigEntity   getGenericConfigEntity (GenericConfigRef  entityRef) { return null; }
    def GenericConfigEntity   findByKey(boolean onlyActive, String tenantId, String configGroup, String configKey) { return null; }
    def List<GenericConfigEntity> findByGroup(boolean onlyActive, String tenantId, String configGroup) { return null; }
    def List<GenericConfigEntity> findByKeyWithDefault(boolean onlyActive, String configGroup, String configKey) { return null; }
}
