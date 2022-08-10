/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.jpa.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import java.util.Objects;

public class TwoTenantIds {

    private final String tenantId1;
    private final String tenantId2;

    public TwoTenantIds(final String tenantId1, final String tenantId2) {
        this.tenantId1 = tenantId1;
        this.tenantId2 = tenantId2;
    }

    public boolean isDoubleGlobal() {
        return T9tConstants.GLOBAL_TENANT_ID.equals(this.tenantId1) && T9tConstants.GLOBAL_TENANT_ID.equals(this.tenantId2);
    }

    public String effectiveTenantId() {
        if (T9tConstants.GLOBAL_TENANT_ID.equals(tenantId1)) {
            return tenantId2;
        }
        return tenantId1;
    }

    public String getTenantId1() {
        return tenantId1;
    }

    public String getTenantId2() {
        return tenantId2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId1, tenantId2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TwoTenantIds other = (TwoTenantIds) obj;
        return Objects.equals(tenantId1, other.tenantId1) && Objects.equals(tenantId2, other.tenantId2);
    }

    @Override
    public String toString() {
        return "(" + tenantId1 + ", " + tenantId2 + ")";
    }
}
