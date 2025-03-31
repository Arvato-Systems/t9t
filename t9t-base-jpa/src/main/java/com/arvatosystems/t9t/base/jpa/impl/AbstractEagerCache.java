/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IEagerCache;
import com.arvatosystems.t9t.base.services.RequestContext;

public abstract class AbstractEagerCache<T> implements IEagerCache<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEagerCache.class);

    protected final Map<String, T> cacheByTenantId = new ConcurrentHashMap<>();
    protected final Function<RequestContext, T> constructor;

    protected AbstractEagerCache(final Function<RequestContext, T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void refreshCache(final RequestContext ctx) {
        cacheByTenantId.put(ctx.tenantId, constructor.apply(ctx));
    }

    @Override
    public T getCache(final RequestContext ctx) {
        return cacheByTenantId.computeIfAbsent(ctx.tenantId, (x) -> {
            LOGGER.warn("No data present for {} for tenant {}", getClass().getSimpleName(), ctx.tenantId);
            return constructor.apply(ctx);
        });
    }
}
