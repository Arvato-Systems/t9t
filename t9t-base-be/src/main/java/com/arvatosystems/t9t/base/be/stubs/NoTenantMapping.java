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
package com.arvatosystems.t9t.base.be.stubs;

import com.arvatosystems.t9t.base.services.ITenantMapping;

public class NoTenantMapping implements ITenantMapping {

    final Long tenantRef;
    final String tenantId;

    public NoTenantMapping(Long tenantRef, String tenantId) {
        this.tenantRef = tenantRef;
        this.tenantId = tenantId;
    }

    @Override
    public Long getSharedTenantRef(int rtti) {
        return tenantRef;
    }

    @Override
    public String getSharedTenantId(int rtti) {
        return tenantId;
    }
}
