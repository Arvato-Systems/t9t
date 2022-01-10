/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.IResolverLongKey42;
import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigDTO;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.core.jpa.entities.ModuleConfigEntity;
import com.arvatosystems.t9t.server.services.IModuleConfigResolver;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableData;
import de.jpaw.dp.Jdp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of a class which reads module tenant configuration (and caches the entries). */
public abstract class AbstractModuleConfigResolver<D extends ModuleConfigDTO, E extends ModuleConfigEntity & BonaPersistableData<D>>
        implements IModuleConfigResolver<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModuleConfigResolver.class);
    private final IResolverLongKey42<FullTrackingWithVersion, E> resolver;

    private String query;

    private final Cache<Long, D> dtoCache;

    private final ICacheInvalidationRegistry cacheInvalidationRegistry = Jdp.getRequired(ICacheInvalidationRegistry.class);

    protected AbstractModuleConfigResolver(final Class<? extends IResolverLongKey42<FullTrackingWithVersion, E>> resolverClass) {
        resolver = Jdp.getRequired(resolverClass);
        dtoCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
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
        final Long tenantRef = resolver.getSharedTenantRef();
        final D cacheHit = dtoCache.getIfPresent(tenantRef);
        if (cacheHit != null) {
          return cacheHit;
        }
        // not in cache: read database
        final EntityManager em = resolver.getEntityManager();
        final List<Long> tenants;
        if (T9tConstants.GLOBAL_TENANT_REF42.equals(tenantRef)) {
            tenants = new ArrayList<>(1);
            tenants.add(T9tConstants.GLOBAL_TENANT_REF42);
        } else {
            tenants = new ArrayList<>(2);
            tenants.add(T9tConstants.GLOBAL_TENANT_REF42);
            tenants.add(tenantRef);
        }
        query = "SELECT e FROM " + resolver.getEntityClass().getSimpleName() + " e WHERE e.tenantRef IN :tenants ORDER BY e.tenantRef DESC";

        D result = getDefaultModuleConfiguration();
        final TypedQuery<E> quer = em.createQuery(query, resolver.getEntityClass());
        quer.setParameter("tenants", tenants);
        try {
            final List<E> results = quer.getResultList();
            if (results != null && !results.isEmpty()) {
                LOGGER.debug("Found entry for ModuleConfigResolver cache {} for tenantRef {}", resolver.getBaseJpaEntityClass().getSimpleName(), tenantRef);
                result = results.get(0).ret$Data();
            } else {
                LOGGER.debug("No entry for ModuleConfigResolver cache {} for tenantRef {}, using defaults!", resolver.getBaseJpaEntityClass().getSimpleName(),
                        tenantRef);
            }
        } catch (final Exception e) {
            LOGGER.error("JPA exception {} while reading module configuration for {} for tenantRef {}: {}", e.getClass().getSimpleName(),
                    resolver.getEntityClass().getSimpleName(), tenantRef, e.getMessage());
            LOGGER.error("Stack trace is ", e);
            throw e;
        }
        result.freeze(); // make immutable
        dtoCache.put(tenantRef, result);
        LOGGER.debug("Updating ModuleConfigResolver cache {} for tenantRef {}", resolver.getBaseJpaEntityClass().getSimpleName(), tenantRef);
        return result;
    }

    @Override
    public void updateModuleConfiguration(final D cfg) {
        final E newEntity = resolver.newEntityInstance();
        final Long tenantRef = resolver.getSharedTenantRef();
        newEntity.put$Data(cfg);
        newEntity.put$Key(tenantRef);
        newEntity.setTenantRef(tenantRef);
        resolver.getEntityManager().<E>merge(newEntity); // resolver.save(newEntity) would do this and some of the previous assignments...
        cfg.freeze();
        dtoCache.put(tenantRef, cfg); // update the cache
    }
}
