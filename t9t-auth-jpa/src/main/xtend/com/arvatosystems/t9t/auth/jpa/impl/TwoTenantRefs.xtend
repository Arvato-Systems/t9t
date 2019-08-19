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
package com.arvatosystems.t9t.auth.jpa.impl

import com.arvatosystems.t9t.base.T9tConstants
import org.eclipse.xtend.lib.annotations.Data

@Data
class TwoTenantRefs implements T9tConstants {
    Long tenantRef1;
    Long tenantRef2;

    def isDoubleGlobal() {
        return GLOBAL_TENANT_REF42 == tenantRef1 && GLOBAL_TENANT_REF42 == tenantRef2
    }
    def effectiveTenantRef() {
        return if (GLOBAL_TENANT_REF42 == tenantRef1) tenantRef2 else tenantRef1
    }
    override toString() {
        return '''(«tenantRef1», «tenantRef2»)'''
    }
}
