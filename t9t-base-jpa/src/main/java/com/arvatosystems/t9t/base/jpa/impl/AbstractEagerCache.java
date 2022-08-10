package com.arvatosystems.t9t.base.jpa.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IEagerCache;
import com.arvatosystems.t9t.base.services.RequestContext;

public abstract class AbstractEagerCache<T> implements IEagerCache<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEagerCache.class);

    protected final Map<String, T> cacheByTenantId = new ConcurrentHashMap<>();
    protected final Function<RequestContext, T> constructor;

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
            LOGGER.warn("No data present for {} for tenant {}", getClass().getSimpleName(), ctx.tenantId);
            return constructor.apply(ctx);
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
