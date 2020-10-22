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
package com.arvatosystems.t9t.core.jpa.impl

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.jpa.IResolverLongKey42
import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigDTO
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry
import com.arvatosystems.t9t.core.jpa.entities.ModuleConfigEntity
import com.arvatosystems.t9t.server.services.IModuleConfigResolver
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.jpa.BonaPersistableData
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import java.util.concurrent.TimeUnit

/** Implementation of a class which reads module tenant configuration (and caches the entries). */
@AddLogger
abstract class AbstractModuleConfigResolver<D extends ModuleConfigDTO, E extends ModuleConfigEntity & BonaPersistableData<D>>
 implements T9tConstants, IModuleConfigResolver<D> {
    final IResolverLongKey42<FullTrackingWithVersion, E> resolver
    String query
    final Cache<Long,D> dtoCache
    @Inject ICacheInvalidationRegistry cacheInvalidationRegistry


    protected new(Class<? extends IResolverLongKey42<FullTrackingWithVersion, E>> resolverClass) {
        resolver = Jdp.getRequired(resolverClass)
        dtoCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build
        cacheInvalidationRegistry.registerInvalidator(resolver.baseJpaEntityClass.simpleName, [ dtoCache.invalidateAll ])
        LOGGER.info("Created ModuleConfigResolver for {}", resolver.baseJpaEntityClass.simpleName)
    }

    /**
     * {@inheritDoc }
     */
    override D getModuleConfiguration() {
        val tenantRef = resolver.sharedTenantRef
        val cacheHit = dtoCache.getIfPresent(tenantRef)
        if (cacheHit !== null)
            return cacheHit;
        // not in cache: read database
        val em = resolver.entityManager
        val tenants = if (GLOBAL_TENANT_REF42.equals(tenantRef)) #[ GLOBAL_TENANT_REF42 ] else #[ GLOBAL_TENANT_REF42, tenantRef ]
        query    = '''SELECT e FROM «resolver.entityClass.simpleName» e WHERE e.tenantRef IN :tenants ORDER BY e.tenantRef DESC'''

        var D result = getDefaultModuleConfiguration
        val quer = em.createQuery(query, resolver.entityClass)
        quer.setParameter("tenants", tenants)
        try {
            val results = quer.resultList
            if (!results.nullOrEmpty) {
                LOGGER.debug("Found entry for ModuleConfigResolver cache {} for tenantRef {}", resolver.baseJpaEntityClass.simpleName, tenantRef)
                result = results.get(0).ret$Data
            } else {
                LOGGER.debug("No entry for ModuleConfigResolver cache {} for tenantRef {}, using defaults!", resolver.baseJpaEntityClass.simpleName, tenantRef)
            }
        } catch (Exception e) {
            LOGGER.error("JPA exception {} while reading module configuration for {} for tenantRef {}: {}",
                e.class.simpleName, resolver.entityClass.simpleName, tenantRef, e.message);
            LOGGER.error("Stack trace is ", e)
            throw e
        }
        result.freeze  // make immutable
        dtoCache.put(tenantRef, result)
        LOGGER.debug("Updating ModuleConfigResolver cache {} for tenantRef {}", resolver.baseJpaEntityClass.simpleName, tenantRef)
        return result
    }

    override updateModuleConfiguration(D cfg) {
        val E newEntity = resolver.newEntityInstance
        val tenantRef = resolver.sharedTenantRef
        newEntity.put$Data(cfg)
        newEntity.put$Key(tenantRef)
        newEntity.tenantRef = tenantRef
        resolver.entityManager.merge(newEntity)   // resolver.save(newEntity) would do this and some of the previous assignments...
        cfg.freeze
        dtoCache.put(tenantRef, cfg)  // update the cache
    }
}
