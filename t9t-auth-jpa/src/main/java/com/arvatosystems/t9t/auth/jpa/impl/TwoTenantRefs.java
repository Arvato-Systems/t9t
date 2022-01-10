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

public class TwoTenantRefs {

    private final Long tenantRef1;
    private final Long tenantRef2;

    public TwoTenantRefs(final Long tenantRef1, final Long tenantRef2) {
        this.tenantRef1 = tenantRef1;
        this.tenantRef2 = tenantRef2;
    }

    public boolean isDoubleGlobal() {
        return T9tConstants.GLOBAL_TENANT_REF42.equals(this.tenantRef1) && T9tConstants.GLOBAL_TENANT_REF42.equals(this.tenantRef2);
    }

    public Long effectiveTenantRef() {
        if (T9tConstants.GLOBAL_TENANT_REF42.equals(tenantRef1)) {
            return tenantRef2;
        }
        return tenantRef1;
    }

    public Long getTenantRef1() {
        return tenantRef1;
    }

    public Long getTenantRef2() {
        return tenantRef2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantRef1, tenantRef2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TwoTenantRefs other = (TwoTenantRefs) obj;
        return Objects.equals(tenantRef1, other.tenantRef1) && Objects.equals(tenantRef2, other.tenantRef2);
    }

    @Override
    public String toString() {
        return "(" + tenantRef1 + ", " + tenantRef2 + ")";
    }
}
