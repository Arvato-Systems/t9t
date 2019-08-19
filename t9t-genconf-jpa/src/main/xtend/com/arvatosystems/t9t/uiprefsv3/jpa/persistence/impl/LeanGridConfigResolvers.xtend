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
package com.arvatosystems.t9t.uiprefsv3.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigRef
import com.arvatosystems.t9t.uiprefsv3.jpa.entities.LeanGridConfigEntity

@AutoResolver42
class LeanGridConfigResolvers {

    @AllCanAccessGlobalTenant  // for DataSinkEntity, everyone can see the global tenant's defaults
    def LeanGridConfigEntity   getLeanGridConfigEntity (LeanGridConfigRef  entityRef, boolean onlyActive) { return null; }
    def LeanGridConfigEntity   findByKey(boolean onlyActive, Long tenantRef, String gridId, Integer variant, Long userRef) { return null; }
}
