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
package com.arvatosystems.t9t.core.jpa.impl;

import java.util.ArrayList;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/** Implementation of a class which reads module tenant configuration (and caches the entries). */
public abstract class AbstractModuleConfigResolver<D extends ModuleConfigDTO, E extends ModuleConfigEntity & BonaPersistableData<D>>
        implements IModuleConfigResolver<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModuleConfigResolver.class);
    private final IResolverStringKey<FullTrackingWithVersion, E> resolver;

    private String query;

    private final Cache<String, D> dtoCache;

    private final ICacheInvalidationRegistry cacheInvalidationRegistry = Jdp.getRequired(ICacheInvalidationRegistry.class);

    protected AbstractModuleConfigResolver(final Class<? extends IResolverStringKey<FullTrackingWithVersion, E>> resolverClass) {
        resolver = Jdp.getRequired(resolverClass);
        dtoCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
        cacheInvalidationRegistry.registerInvalidator(resolver.getBaseJpaEntityClass().getSimpleName(), (final BonaPortable it) -> {
            dtoCache.invalidateAll();
        });
        LOGGER.info("Created ModuleConfigResolver for {}", resolver.getBaseJpaEntityClass().getSimpleName());
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
        // not in cache: read database
        final EntityManager em = resolver.getEntityManager();
        final List<String> tenants;
        if (T9tConstants.GLOBAL_TENANT_ID.equals(tenantId)) {
            tenants = new ArrayList<>(1);
            tenants.add(T9tConstants.GLOBAL_TENANT_ID);
        } else {
            tenants = new ArrayList<>(2);
            tenants.add(T9tConstants.GLOBAL_TENANT_ID);
            tenants.add(tenantId);
        }
        query = "SELECT e FROM " + resolver.getEntityClass().getSimpleName() + " e WHERE e.tenantId IN :tenants ORDER BY e.tenantId DESC";

        D result = getDefaultModuleConfiguration();
        final TypedQuery<E> quer = em.createQuery(query, resolver.getEntityClass());
        quer.setParameter("tenants", tenants);
        try {
            final List<E> results = quer.getResultList();
            if (results != null && !results.isEmpty()) {
                LOGGER.debug("Found entry for ModuleConfigResolver cache {} for tenantId {}", resolver.getBaseJpaEntityClass().getSimpleName(), tenantId);
                result = results.get(0).ret$Data();
            } else {
                LOGGER.debug("No entry for ModuleConfigResolver cache {} for tenantId {}, using defaults!", resolver.getBaseJpaEntityClass().getSimpleName(),
                        tenantId);
            }
        } catch (final Exception e) {
            LOGGER.error("JPA exception {} while reading module configuration for {} for tenantId {}: {}", e.getClass().getSimpleName(),
                    resolver.getEntityClass().getSimpleName(), tenantId, e.getMessage());
            LOGGER.error("Stack trace is ", e);
            throw e;
        }
        result.freeze(); // make immutable
        dtoCache.put(tenantId, result);
        LOGGER.debug("Updating ModuleConfigResolver cache {} for tenantId {}", resolver.getBaseJpaEntityClass().getSimpleName(), tenantId);
        return result;
    }

    @Override
    public void updateModuleConfiguration(final D cfg) {
        final E newEntity = resolver.newEntityInstance();
        final String tenantId = resolver.getSharedTenantId();
        newEntity.put$Data(cfg);
        newEntity.put$Key(tenantId);
        newEntity.setTenantId(tenantId);
        resolver.getEntityManager().<E>merge(newEntity); // resolver.save(newEntity) would do this and some of the previous assignments...
        cfg.freeze();
        dtoCache.put(tenantId, cfg); // update the cache
    }
}
