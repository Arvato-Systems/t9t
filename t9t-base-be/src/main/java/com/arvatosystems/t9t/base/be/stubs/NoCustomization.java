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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.ICustomization;
import com.arvatosystems.t9t.base.services.ITenantCustomization;
import com.arvatosystems.t9t.base.services.ITenantMapping;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Any
@Singleton
public class NoCustomization implements ICustomization {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoCustomization.class);
    private final ITenantCustomization myCustomization = new NoTenantCustomization();

    public NoCustomization() {
        LOGGER.warn("NoCustomization implementation selected - all tenants will use the core data model");
    }

    @Override
    public ITenantCustomization getTenantCustomization(final Long tenantRef, final String tenantId) {
        return myCustomization;
    }

    @Override
    public ITenantMapping getTenantMapping(Long tenantRef, String tenantId) {
        return new NoTenantMapping(tenantRef, tenantId);
    }
}
