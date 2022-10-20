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
package com.arvatosystems.t9t.base.services;

/**
 * Generic Interface which describes the implementation for the factory of eager cache instances.
 * For every relevant module, one specific implementation will be defined.
 */
public interface IEagerCache<T> {

    /**
     * Refreshes the cache for the current tenant.
     *
     * @param ctx the {@link RequestContext} of this operation
     */
    void refreshCache(RequestContext ctx);

    /**
     * Obtains the cache contents for the current tenant.
     * If no entry exists, it is created.
     *
     * @param ctx the {@link RequestContext} of this operation
     * @return the cache data for the tenant stored within the context.
     **/
    T getCache(RequestContext ctx);

    /**
     * Obtains the cache contents for the current tenant.
     * If no entry exists, an exception is thrown.
     *
     * @param tenantId the tenant reference for this operation
     * @return the cache data for the specified tenant.
     **/
    T getCache(String tenantId);
}
