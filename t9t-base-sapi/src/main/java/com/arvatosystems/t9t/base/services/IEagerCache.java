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
