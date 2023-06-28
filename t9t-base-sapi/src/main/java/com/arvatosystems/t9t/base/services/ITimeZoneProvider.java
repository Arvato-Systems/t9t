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
package com.arvatosystems.t9t.base.services;

import java.time.ZoneId;

/**
 * Retrieves the time zone of the specified tenant.
 */
@FunctionalInterface
public interface ITimeZoneProvider {

    /**
     * Provides the time zone of the current tenant.
     *
     * A fallback implementation will read a global default from the system configuration. This is used when no database is available.
     * The improved implementation reads it from the tenant configuration.
     * Both implementations will return ZoneOffset.UTC  if no time zone hasd been configured.
     *
     * @param tenantId          specifies the tenant
     * @return                  the ZoneId
     */
    ZoneId getTimeZoneOfTenant(String tenantId);
}
