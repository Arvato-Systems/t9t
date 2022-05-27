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

    protected final Map<Long, T> cacheByTenantRef = new ConcurrentHashMap<>();
    protected final Function<RequestContext, T> constructor;

    protected AbstractEagerCache(Function<RequestContext, T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void refreshCache(RequestContext ctx) {
        cacheByTenantRef.put(ctx.tenantRef, constructor.apply(ctx));
    }

    @Override
    public T getCache(RequestContext ctx) {
        return cacheByTenantRef.computeIfAbsent(ctx.tenantRef, (x) -> {
            LOGGER.warn("No data present for {} for tenant {}", getClass().getSimpleName(), ctx.tenantId);
            return constructor.apply(ctx);
        });
    }

    @Override
    public T getCache(Long tenantRef) {
        final T cache = cacheByTenantRef.get(tenantRef);
        if (cache == null) {
            final String cacheType = getClass().getSimpleName();
            LOGGER.error("No data present for {} for tenant {}", cacheType, tenantRef);
            throw new T9tException(T9tException.NO_DATA_CACHED, cacheType, tenantRef);
        }
        return cache;
    }
}
