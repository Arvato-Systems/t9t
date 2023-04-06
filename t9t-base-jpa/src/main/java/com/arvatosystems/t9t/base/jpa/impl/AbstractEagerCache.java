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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IEagerCache;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import jakarta.persistence.EntityManager;

public abstract class AbstractEagerCache<T> implements IEagerCache<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEagerCache.class);

    protected final Map<String, T> cacheByTenantId = new ConcurrentHashMap<>();
    protected final Function<RequestContext, T> constructor;
    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    protected AbstractEagerCache(Function<RequestContext, T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void refreshCache(RequestContext ctx) {
        cacheByTenantId.put(ctx.tenantId, constructor.apply(ctx));
    }

    @Override
    public T getCache(RequestContext ctx) {
        return cacheByTenantId.computeIfAbsent(ctx.tenantId, (x) -> {
            LOGGER.warn("No data present for {} for tenant {}, now doing em.flush() and load", getClass().getSimpleName(), ctx.tenantId);
            final EntityManager em = jpaContextProvider.get().getEntityManager();
            em.flush();  // save any pending changes, because we want to clear L1 after the load
            T cache = constructor.apply(ctx);
            em.clear();  // clear potentially huge L1 cache
            LOGGER.info("Completed eager cache loading for {} for tenant {} - em.clear done", getClass().getSimpleName(), ctx.tenantId);
            return cache;
        });
    }

    @Override
    public T getCache(String tenantId) {
        final T cache = cacheByTenantId.get(tenantId);
        if (cache == null) {
            final String cacheType = getClass().getSimpleName();
            LOGGER.error("No data present for {} for tenant {}", cacheType, tenantId);
            throw new T9tException(T9tException.NO_DATA_CACHED, cacheType, tenantId);
        }
        return cache;
    }
}
