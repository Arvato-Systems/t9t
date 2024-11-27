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
package com.arvatosystems.t9t.core.jpa.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.IResolverStringKey;
import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigDTO;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.core.jpa.entities.ModuleConfigEntity;
import com.arvatosystems.t9t.server.services.IModuleConfigResolver;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableData;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/** Implementation of a class which reads module tenant configuration (and caches the entries). */
public abstract class AbstractModuleConfigResolver<D extends ModuleConfigDTO, E extends ModuleConfigEntity & BonaPersistableData<D>>
        implements IModuleConfigResolver<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModuleConfigResolver.class);

    private final IResolverStringKey<FullTrackingWithVersion, E> resolver;
    private final Cache<String, D> dtoCache;
    private final ICacheInvalidationRegistry cacheInvalidationRegistry = Jdp.getRequired(ICacheInvalidationRegistry.class);

    protected AbstractModuleConfigResolver(final Class<? extends IResolverStringKey<FullTrackingWithVersion, E>> resolverClass) {
        resolver = Jdp.getRequired(resolverClass);
        dtoCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
        cacheInvalidationRegistry.registerInvalidator(resolver.getBaseJpaEntityClass().getSimpleName(), (final BonaPortable it) -> {
            LOGGER.info("Invalidating ModuleCfg cache for {}", this.getClass().getSimpleName());  // will expand to the specific class name
            dtoCache.invalidateAll();
        });
        LOGGER.info("Created ModuleConfigResolver for {}", resolver.getBaseJpaEntityClass().getSimpleName());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public D getUncachedModuleConfiguration() {
        final String tenantId = resolver.getSharedTenantId();
        return getModuleConfiguration(tenantId);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public D getModuleConfiguration() {
        final String tenantId = resolver.getSharedTenantId();
        final D cacheHit = dtoCache.getIfPresent(tenantId);
        if (cacheHit != null) {
            return cacheHit;
        }
        final D result = getModuleConfiguration(tenantId);
        dtoCache.put(tenantId, result);
        return result;
    }

    protected D getModuleConfiguration(@Nonnull final String tenantId) {
        // not in cache: read database
        final EntityManager em = resolver.getEntityManager();
        final List<String> tenants;
        if (T9tConstants.GLOBAL_TENANT_ID.equals(tenantId)) {
            tenants = List.of(T9tConstants.GLOBAL_TENANT_ID);
        } else {
            tenants = List.of(T9tConstants.GLOBAL_TENANT_ID, tenantId);
        }

        final String sql = "SELECT e FROM " + resolver.getEntityClass().getSimpleName() + " e WHERE e.tenantId IN :tenants ORDER BY e.tenantId DESC";
        final TypedQuery<E> query = em.createQuery(sql, resolver.getEntityClass());
        query.setParameter("tenants", tenants);
        try {
            final D result;
            final List<E> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                LOGGER.debug("Found entry for ModuleConfigResolver cache {} for tenantId {} - updating cache with it",
                    resolver.getBaseJpaEntityClass().getSimpleName(), tenantId);
                result = e2d(results.get(0));
            } else {
                LOGGER.debug("No entry for ModuleConfigResolver cache {} for tenantId {}, - updating cache with defaults",
                    resolver.getBaseJpaEntityClass().getSimpleName(), tenantId);
                result = getDefaultModuleConfiguration();
            }
            result.freeze(); // make immutable
            return result;
        } catch (final Exception e) {
            LOGGER.error("JPA exception {} while reading module configuration for {} for tenantId {}: {}", e.getClass().getSimpleName(),
                    resolver.getEntityClass().getSimpleName(), tenantId, e.getMessage());
            LOGGER.error("Stack trace is ", e);
            throw e;
        }
    }

    @Override
    public void updateModuleConfiguration(final D cfg) {
        final E newEntity = resolver.newEntityInstance();
        final String tenantId = resolver.getSharedTenantId();
        d2e(newEntity, cfg);
        newEntity.put$Key(tenantId);
        newEntity.setTenantId(tenantId);
        resolver.getEntityManager().<E>merge(newEntity); // resolver.save(newEntity) would do this and some of the previous assignments...
        cfg.freeze();
        dtoCache.put(tenantId, cfg); // update the cache
    }

    /**
     * Overridable method to provide entity to DTO mapping.
     * Most module configurations do not need a full blown mapper, therefore a default implementation is provided.
     *
     * @param entity the entity to convert
     * @return the mapped DTO
     */
    protected D e2d(final E entity) {
        return entity.ret$Data();
    }

    /**
     * Overridable method to provide DTO to entity mapping.
     * Most module configurations do not need a full blown mapper, therefore a default implementation is provided.
     *
     * @param dst a preallocated entity object to map into
     * @param src the source DTO
     */
    protected void d2e(final E dst, final D src) {
        dst.put$Data(src);
    }
}
